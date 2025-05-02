package com.Skysync.business;

import com.Skysync.events.WeatherEvent;
import com.Skysync.models.Clima;
import com.Skysync.models.Vuelo;
import com.google.gson.Gson;
import org.apache.activemq.ActiveMQConnectionFactory;
import com.Skysync.config.AppConfig;


import javax.jms.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.List;
import java.util.ArrayList;

public class BusinessUnit implements MessageListener {

	private final Gson gson = new Gson();
	private final DatamartManager datamartManager = new DatamartManager();
	private ExecutorService executorService;

	String WEATHER_TOPIC = AppConfig.get("WEATHER_TOPIC");

	String BROKER_URL = AppConfig.get("BROKER_URL");

	double UMBRAL_VELOCIDAD_VIENTO = AppConfig.getDouble("UMBRAL_VELOCIDAD_VIENTO");
	double UMBRAL_HUMEDAD = AppConfig.getDouble("UMBRAL_HUMEDAD");

	private boolean esClimaExtremo(Clima clima) {
		return clima.getVelocidadViento() > UMBRAL_VELOCIDAD_VIENTO ||
				clima.getHumedad() > UMBRAL_HUMEDAD;
	}

	public void iniciar() {
		executorService = Executors.newSingleThreadExecutor();
		executorService.submit(this::suscribirseEventos);
	}

	private void suscribirseEventos() {
		try {
			ConnectionFactory factory = new ActiveMQConnectionFactory(BROKER_URL);
			Connection connection = factory.createConnection();
			connection.setClientID("SkySync-BusinessUnit");
			connection.start();

			Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

			Topic topicWeather = session.createTopic(WEATHER_TOPIC);
			Topic topicFlight = session.createTopic("prediction.Flight"); // <-- Nuevo topic

			MessageConsumer consumerWeather = session.createDurableSubscriber(topicWeather, "DurableBusinessUnitWeather");
			MessageConsumer consumerFlight = session.createDurableSubscriber(topicFlight, "DurableBusinessUnitFlight");

			consumerWeather.setMessageListener(this);
			consumerFlight.setMessageListener(this);

			System.out.println("📡 BusinessUnit escuchando eventos de clima y vuelos...");

			synchronized (this) {
				this.wait();
			}

		} catch (Exception e) {
			System.out.println("❌ Error conectando BusinessUnit:");
			e.printStackTrace();
		}
	}

	@Override
	public void onMessage(Message message) {
		if (message instanceof TextMessage textMessage) {
			try {
				String json = textMessage.getText();

				if (json.contains("temperatura") && json.contains("humedad")) {
					procesarEventoClima(json);
				} else if (json.contains("numeroVuelo") && json.contains("estado")) {
					procesarEventoVuelo(json);
				} else {
					System.out.println("⚠️ Evento no reconocido: " + json);
				}

			} catch (Exception e) {
				System.out.println("⚠️ Error procesando evento general:");
				e.printStackTrace();
			}
		}
	}


	public void cargarEventosHistoricos() {
		List<File> archivosEventos = new ArrayList<>();

		// Carpetas donde buscar
		archivosEventos.addAll(listarArchivos(new File("eventstore/prediction.Weather/feederA")));
		archivosEventos.addAll(listarArchivos(new File("eventstore/prediction.Flight/feederB")));

		System.out.println("\n📂 Cargando eventos históricos...");

		for (File archivo : archivosEventos) {
			try (BufferedReader br = new BufferedReader(new FileReader(archivo))) {
				String linea;
				while ((linea = br.readLine()) != null) {
					procesarEventoHistorico(linea);
				}
			} catch (Exception e) {
				System.out.println("❌ Error leyendo archivo: " + archivo.getName());
				e.printStackTrace();
			}
		}

		System.out.println("✅ Carga histórica completada.");
	}



	private List<File> listarArchivos(File carpeta) {
		List<File> archivos = new ArrayList<>();
		if (carpeta.exists() && carpeta.isDirectory()) {
			File[] archivosEncontrados = carpeta.listFiles((dir, name) -> name.endsWith(".events"));
			if (archivosEncontrados != null) {
				archivos.addAll(List.of(archivosEncontrados));
			}
		}
		return archivos;
	}

	private void procesarEventoHistorico(String linea) {
		try {
			if (linea.contains("temperatura") && linea.contains("humedad")) {
				// Es clima
				WeatherEvent evento = gson.fromJson(linea, WeatherEvent.class);
				Clima clima = evento.getData();
				datamartManager.insertarClima(clima);
			} else if (linea.contains("numeroVuelo") && linea.contains("estado")) {
				// Es vuelo
				Vuelo vuelo = gson.fromJson(linea, Vuelo.class);
				datamartManager.insertarVuelo(vuelo);
			}
		} catch (Exception e) {
			System.out.println("⚠️ Error procesando línea histórica:");
			e.printStackTrace();
		}
	}

	public void lanzarAlertaCombinada() {
		int climasExtremos = datamartManager.contarClimasExtremos();
		int vuelosRetrasados = datamartManager.contarVuelosRetrasadosActuales();

		System.out.println("\n🔎 Evaluando condiciones combinadas...");

		if (climasExtremos > 0 && vuelosRetrasados > 3) { // puedes ajustar el "3" como umbral
			System.out.println("🚨 ALERTA COMBINADA:");
			System.out.println("- Climas extremos detectados: " + climasExtremos);
			System.out.println("- Vuelos retrasados detectados: " + vuelosRetrasados);
			System.out.println("➡️ Riesgo elevado de cancelaciones y congestión aérea.");
		} else {
			System.out.println("✅ No se detectan riesgos combinados significativos ahora mismo.");
		}
	}


	public void mostrarResumen() {
		datamartManager.mostrarResumenClimaPorCiudad();
	}

	public void detectarCondicionesExtremas() {
		datamartManager.detectarCondicionesExtremas();
	}

	public void mostrarEstadoVuelos() {
		datamartManager.mostrarEstadoVuelos();
	}

	public String resumenComoTexto() {
		StringBuilder sb = new StringBuilder();
		String sql = """
        SELECT ciudad,
               AVG(temperatura) AS temp_media,
               AVG(humedad) AS humedad_media,
               AVG(velocidadViento) AS viento_medio
        FROM clima_datamart
        GROUP BY ciudad
        ORDER BY ciudad;
    """;

		try (var conn = datamartManager.connect();
			 var stmt = conn.prepareStatement(sql);
			 var rs = stmt.executeQuery()) {

			sb.append("📊 Resumen de Clima Promedio:\n");
			while (rs.next()) {
				sb.append(String.format("- %s → 🌡️ %.1f°C, 💧 %.0f%%, 💨 %.1f km/h\n",
						rs.getString("ciudad"),
						rs.getDouble("temp_media"),
						rs.getDouble("humedad_media"),
						rs.getDouble("viento_medio")));
			}
		} catch (Exception e) {
			return "❌ Error consultando clima promedio.";
		}
		return sb.toString();
	}

	public String estadoVuelosComoTexto() {
		StringBuilder sb = new StringBuilder();
		String sqlRetrasados = """
        SELECT COUNT(*) AS total
        FROM vuelos_datamart
        WHERE estado LIKE '%delay%';
    """;

		String sqlCancelados = """
        SELECT COUNT(*) AS total
        FROM vuelos_datamart
        WHERE estado LIKE '%cancel%';
    """;

		try (var conn = datamartManager.connect()) {
			var stmtDelay = conn.prepareStatement(sqlRetrasados);
			var stmtCancel = conn.prepareStatement(sqlCancelados);

			var rsDelay = stmtDelay.executeQuery();
			var rsCancel = stmtCancel.executeQuery();

			int totalDelays = rsDelay.next() ? rsDelay.getInt("total") : 0;
			int totalCanceled = rsCancel.next() ? rsCancel.getInt("total") : 0;

			sb.append("🛬 Estado actual de vuelos:\n");
			sb.append("- ✈️ Vuelos retrasados: ").append(totalDelays).append("\n");
			sb.append("- 🛑 Vuelos cancelados: ").append(totalCanceled).append("\n");

		} catch (Exception e) {
			return "❌ Error consultando estado de vuelos.";
		}
		return sb.toString();
	}

	public String condicionesExtremasComoTexto() {
		StringBuilder sb = new StringBuilder();
		String sql = """
        SELECT ciudad, temperatura, humedad, velocidadViento, condicion
        FROM clima_datamart
        WHERE velocidadViento > 30
           OR humedad > 90;
    """;

		try (var conn = datamartManager.connect();
			 var stmt = conn.prepareStatement(sql);
			 var rs = stmt.executeQuery()) {

			boolean encontrado = false;
			sb.append("🌩️ Condiciones Meteorológicas Extremas:\n");
			while (rs.next()) {
				encontrado = true;
				sb.append(String.format("- %s → 🌡️ %.1f°C, 💧 %.0f%%, 💨 %.1f km/h, ☁️ %s\n",
						rs.getString("ciudad"),
						rs.getDouble("temperatura"),
						rs.getDouble("humedad"),
						rs.getDouble("velocidadViento"),
						rs.getString("condicion")));
			}
			if (!encontrado) {
				return "✅ No se detectaron condiciones extremas.";
			}
		} catch (Exception e) {
			return "❌ Error consultando condiciones extremas.";
		}
		return sb.toString();
	}

	public String alertaCombinadaComoTexto() {
		int climasExtremos = datamartManager.contarClimasExtremos();
		int vuelosRetrasados = datamartManager.contarVuelosRetrasadosActuales();

		if (climasExtremos > 0 && vuelosRetrasados > 3) {
			return String.format("🚨 ALERTA: %d climas extremos y %d vuelos retrasados detectados.\nRiesgo elevado de congestión aérea.", climasExtremos, vuelosRetrasados);
		} else {
			return "✅ No se detectan riesgos combinados importantes en este momento.";
		}
	}

	private void procesarEventoClima(String json) {
		try {
			WeatherEvent evento = gson.fromJson(json, WeatherEvent.class);
			Clima clima = evento.getData();
			datamartManager.insertarClima(clima);

			System.out.println("✅ Clima procesado y guardado: " + clima);

			if (esClimaExtremo(clima)) {
				System.out.println("🚨 ALERTA INMEDIATA: Condiciones extremas detectadas en " + clima.getCiudad());
				System.out.printf("Detalles: 🌡️ %.1f°C, 💧 %.0f%%, 💨 %.1f km/h, ☁️ %s%n",
						clima.getTemperatura(), clima.getHumedad(), clima.getVelocidadViento(), clima.getCondicion());
			}
		} catch (Exception e) {
			System.out.println("⚠️ Error procesando evento de clima:");
			e.printStackTrace();
		}
	}

	private void procesarEventoVuelo(String json) {
		try {
			Vuelo vuelo = gson.fromJson(json, Vuelo.class);
			datamartManager.insertarVuelo(vuelo);
			System.out.println("✅ Vuelo procesado y guardado: " + vuelo.getNumeroVuelo());
		} catch (Exception e) {
			System.out.println("⚠️ Error procesando evento de vuelo:");
			e.printStackTrace();
		}
	}



}
