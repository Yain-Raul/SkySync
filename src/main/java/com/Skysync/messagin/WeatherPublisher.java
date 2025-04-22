package com.Skysync.messagin;

import com.Skysync.events.WeatherEvent;
import com.Skysync.main.Config;
import com.google.gson.Gson;
import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;

public class WeatherPublisher {
	private static final String BROKER_URL = Config.BROKER_URL;
	private static final String TOPIC_NAME = Config.WEATHER_TOPIC;
	private final Gson gson = new Gson();

	public void publicar(WeatherEvent evento) {
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

			System.out.println("📤 Evento enviado: " + json);

			producer.close();
			session.close();
			connection.close();
		} catch (Exception e) {
			System.out.println("❌ Error al enviar el evento:");
			e.printStackTrace();
		}
	}
}
