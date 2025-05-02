package com.Skysync.feeders.flights;

import com.google.gson.Gson;
import com.Skysync.events.FlightEvent;
import com.Skysync.config.AppConfig;
import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;

public class FlightPublisher {
	private static final String BROKER_URL = AppConfig.get("BROKER_URL");
	private static final String TOPIC_NAME = "prediction.Flight";
	private final Gson gson = new Gson();

	public void publicar(FlightEvent evento) {
		try {
			ConnectionFactory factory = new ActiveMQConnectionFactory(BROKER_URL);
			Connection connection = factory.createConnection();
			connection.start();

			Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			Topic topic = session.createTopic(TOPIC_NAME);
			MessageProducer producer = session.createProducer(topic);

			String json = gson.toJson(evento);
			TextMessage message = session.createTextMessage(json);
			producer.send(message);

			System.out.println("📤 Evento de vuelo enviado: " + json);

			producer.close();
			session.close();
			connection.close();
		} catch (Exception e) {
			System.out.println("❌ Error al enviar evento de vuelo:");
			e.printStackTrace();
		}
	}
}
