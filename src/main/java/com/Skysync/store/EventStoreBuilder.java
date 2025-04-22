package com.Skysync.store;

import com.Skysync.events.WeatherEvent;
import com.Skysync.main.Config;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class EventStoreBuilder implements MessageListener {

	private static final String BROKER_URL = Config.BROKER_URL;
	private static final String TOPIC_NAME = Config.WEATHER_TOPIC;
	private static final String SOURCE = Config.WEATHER_SOURCE;
	private static final String EVENTSTORE_DIR = "eventstore";
	private final Gson gson = new Gson();

	public void iniciar() {
		try {
			ConnectionFactory factory = new ActiveMQConnectionFactory(BROKER_URL);
			Connection connection = factory.createConnection();
			connection.setClientID("SkySync-EventStoreBuilder");
			connection.start();

			Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			Topic topic = session.createTopic(TOPIC_NAME);
			MessageConsumer consumer = session.createDurableSubscriber(topic, "DurableWeatherSubscriber");

			consumer.setMessageListener(this);

			System.out.println("📥 EventStoreBuilder escuchando eventos...");

			synchronized (this) {
				this.wait();
			}
		} catch (Exception e) {
			System.out.println("❌ No se pudo conectar al broker: " + e.getMessage());
		}
	}

	@Override
	public void onMessage(Message message) {
		if (message instanceof TextMessage) {
			try {
				String json = ((TextMessage) message).getText();
				WeatherEvent evento = gson.fromJson(json, WeatherEvent.class);

				guardarEvento(evento);
				System.out.println("✅ Evento guardado: " + json);

			} catch (JMSException | JsonSyntaxException e) {
				System.out.println("⚠️ Error procesando evento:");
				e.printStackTrace();
			}
		}
	}

	private void guardarEvento(WeatherEvent evento) {
		try {
			LocalDate fecha = LocalDate.parse(evento.getTs().substring(0, 10));
			String fechaStr = fecha.format(DateTimeFormatter.ofPattern("yyyyMMdd"));

			String ruta = Paths.get(EVENTSTORE_DIR, TOPIC_NAME, evento.getSs()).toString();
			File directorio = new File(ruta);
			if (!directorio.exists()) directorio.mkdirs();

			File archivo = new File(directorio, fechaStr + ".events");
			try (PrintWriter pw = new PrintWriter(new FileWriter(archivo, true))) {
				pw.println(gson.toJson(evento));
			}
		} catch (Exception e) {
			System.out.println("❌ Error al guardar evento:");
			e.printStackTrace();
		}
	}
}
