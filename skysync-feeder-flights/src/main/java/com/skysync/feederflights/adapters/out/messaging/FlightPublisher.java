package com.skysync.feederflights.adapters.out.messaging;

import com.google.gson.Gson;
import com.skysync.core.domain.events.FlightEvent;
import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;
import java.util.UUID;

public class FlightPublisher {

	private static final String BROKER_URL = "tcp://localhost:61616";
	private static final String TOPIC = "prediction.Flight";
	private final Gson gson = new Gson();
	private final Connection connection;
	private final Session session;
	private final MessageProducer producer;

	public FlightPublisher() throws JMSException {
		ConnectionFactory factory = new ActiveMQConnectionFactory(BROKER_URL);
		this.connection = factory.createConnection();
		this.connection.start();
		this.session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		Topic topic = session.createTopic(TOPIC);
		this.producer = session.createProducer(topic);
		// Enable persistence for messages
		this.producer.setDeliveryMode(DeliveryMode.PERSISTENT);
	}

	public void publicar(FlightEvent evento) throws JMSException {
		try {
			String json = gson.toJson(evento);
			TextMessage message = session.createTextMessage(json);
			// Add a unique message ID to help with deduplication
			message.setStringProperty("MessageId", UUID.randomUUID().toString());
			producer.send(message);
			System.out.println("📤 Evento de vuelo publicado: " + json);
		} catch (JMSException e) {
			System.out.println("❌ Error publicando vuelo: " + e.getMessage());
			e.printStackTrace();
			throw e;
		}
	}

	public void close() throws JMSException {
		if (producer != null) producer.close();
		if (session != null) session.close();
		if (connection != null) connection.close();
	}
}