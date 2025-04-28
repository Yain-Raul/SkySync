package com.Skysync.business;

import com.Skysync.events.WeatherEvent;
import com.Skysync.models.Clima;
import com.Skysync.models.Vuelo;
import com.google.gson.Gson;
import org.apache.activemq.ActiveMQConnectionFactory;
import static com.Skysync.main.Config.UMBRAL_HUMEDAD;
import static com.Skysync.main.Config.UMBRAL_VELOCIDAD_VIENTO;

import javax.jms.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.List;
import java.util.ArrayList;



import static com.Skysync.main.Config.BROKER_URL;
import static com.Skysync.main.Config.WEATHER_TOPIC;

public class BusinessUnit implements MessageListener {

	private final Gson gson = new Gson();
	private final DatamartManager datamartManager = new DatamartManager();
	private ExecutorService executorService;

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
					// Es evento de clima
					WeatherEvent evento = gson.fromJson(json, WeatherEvent.class);
					Clima clima = evento.getData();
					datamartManager.insertarClima(clima);

					System.out.println("✅ Clima procesado y guardado: " + clima);

					if (esClimaExtremo(clima)) {
						System.out.println("🚨 ALERTA INMEDIATA: Condiciones extremas detectadas en " + clima.getCiudad());
						System.out.printf("Detalles: 🌡️ %.1f°C, 💧 %.0f%%, 💨 %.1f km/h, ☁️ %s%n",
								clima.getTemperatura(), clima.getHumedad(), clima.getVelocidadViento(), clima.getCondicion());
					}

				} else if (json.contains("numeroVuelo") && json.contains("estado")) {
					// Es evento de vuelo
					Vuelo vuelo = gson.fromJson(json, Vuelo.class);
					datamartManager.insertarVuelo(vuelo);

					System.out.println("✅ Vuelo procesado y guardado: " + vuelo.getNumeroVuelo());
				}

			} catch (Exception e) {
				System.out.println("⚠️ Error procesando evento en BusinessUnit:");
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




}
