package com.skysync.feederflights.adapters.out.messaging;

import com.google.gson.Gson;
import com.skysync.core.domain.events.FlightEvent;
import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;

public class FlightPublisher {

	private static final String BROKER_URL = "tcp://localhost:61616";
	private static final String TOPIC = "prediction.Flight";
	private final Gson gson = new Gson();

	public void publicar(FlightEvent evento) {
		try {
			ConnectionFactory factory = new ActiveMQConnectionFactory(BROKER_URL);
			Connection connection = factory.createConnection();
			connection.start();

			Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			Topic topic = session.createTopic(TOPIC);
			MessageProducer producer = session.createProducer(topic);

			String json = gson.toJson(evento);
			TextMessage message = session.createTextMessage(json);
			producer.send(message);

			System.out.println("📤 Evento de vuelo publicado: " + json);

			producer.close();
			session.close();
			connection.close();

		} catch (Exception e) {
			System.out.println("❌ Error publicando vuelo:");
			e.printStackTrace();
		}
	}
}
