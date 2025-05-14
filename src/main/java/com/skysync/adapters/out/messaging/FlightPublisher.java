package com.skysync.adapters.out.messaging;

import com.google.gson.Gson;
import com.skysync.domain.events.FlightEvent;
import com.skysync.infrastructure.config.AppConfig;
import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;

public class FlightPublisher {

	private static final String BROKER_URL = AppConfig.get("BROKER_URL");
	private static final String TOPIC_NAME = "prediction.Flight";
	private final Gson gson = new Gson();

	public void publicar(FlightEvent evento) {
		try {
			// Crear conexión con ActiveMQ
			ConnectionFactory factory = new ActiveMQConnectionFactory(BROKER_URL);
			Connection connection = factory.createConnection();
			connection.start();

			// Crear sesión
			Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			Topic topic = session.createTopic(TOPIC_NAME);

			// Crear productor y mensaje
			MessageProducer producer = session.createProducer(topic);
			String json = gson.toJson(evento);
			TextMessage message = session.createTextMessage(json);

			// Enviar
			producer.send(message);
			System.out.println("📤 Evento de vuelo enviado: " + json);

			// Cerrar recursos
			producer.close();
			session.close();
			connection.close();

		} catch (Exception e) {
			System.out.println("❌ Error al publicar evento de vuelo:");
			e.printStackTrace();
		}
	}
}
