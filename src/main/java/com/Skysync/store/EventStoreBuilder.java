package com.Skysync.store;

import com.Skysync.events.WeatherEvent;
import com.Skysync.config.AppConfig;
import com.google.gson.Gson;
import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.nio.file.Paths;

import com.Skysync.events.FlightEvent;

public class EventStoreBuilder implements MessageListener {

	String BROKER_URL = AppConfig.get("BROKER_URL");
	String WEATHER_TOPIC = AppConfig.get("WEATHER_TOPIC");
	private static final String SOURCE =  AppConfig.get("WEATHER_SOURCE");
	private static final String EVENTSTORE_DIR = "eventstore";
	private final Gson gson = new Gson();

	public void iniciar() {
		try {
			ConnectionFactory factory = new ActiveMQConnectionFactory(BROKER_URL);
			Connection connection = factory.createConnection();
			connection.setClientID("SkySync-EventStoreBuilder");
			connection.start();

			Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

			Topic weatherTopic = session.createTopic(WEATHER_TOPIC);
			Topic flightTopic = session.createTopic("prediction.Flight");

			MessageConsumer consumerWeather = session.createDurableSubscriber(weatherTopic, "DurableWeatherSubscriber");
			MessageConsumer consumerFlight = session.createDurableSubscriber(flightTopic, "DurableFlightSubscriber");

			consumerWeather.setMessageListener(this);
			consumerFlight.setMessageListener(this);

			System.out.println("📥 EventStoreBuilder escuchando eventos de clima y vuelos...");

			synchronized (this) {
				this.wait();
			}
		} catch (Exception e) {
			System.out.println("❌ No se pudo conectar al broker: " + e.getMessage());
			e.printStackTrace();
		}
	}


	@Override
	public void onMessage(Message message) {
		if (message instanceof TextMessage) {
			try {
				String json = ((TextMessage) message).getText();

				if (json.contains("temperatura") && json.contains("humedad")) {
					WeatherEvent evento = gson.fromJson(json, WeatherEvent.class);
					guardarEvento("prediction.Weather", evento.getSs(), evento.getTs(), json);
					System.out.println("✅ Evento de clima guardado.");
				} else if (json.contains("numeroVuelo") && json.contains("estado")) {
					FlightEvent evento = gson.fromJson(json, FlightEvent.class);
					guardarEvento("prediction.Flight", evento.getSs(), evento.getTs(), json);
					System.out.println("✅ Evento de vuelo guardado.");
				} else {
					System.out.println("⚠️ Tipo de evento no reconocido.");
				}

			} catch (Exception e) {
				System.out.println("⚠️ Error procesando evento:");
				e.printStackTrace();
			}
		}
	}


	private void guardarEvento(String topic, String ss, String timestamp, String json) {
		try {
			String fechaStr = timestamp.substring(0, 10).replace("-", "");
			String ruta = Paths.get(EVENTSTORE_DIR, topic, ss).toString();

			File directorio = new File(ruta);
			if (!directorio.exists()) directorio.mkdirs();

			File archivo = new File(directorio, fechaStr + ".events");
			try (PrintWriter pw = new PrintWriter(new FileWriter(archivo, true))) {
				pw.println(json);
			}
		} catch (Exception e) {
			System.out.println("❌ Error al guardar evento:");
			e.printStackTrace();
		}
	}
}
