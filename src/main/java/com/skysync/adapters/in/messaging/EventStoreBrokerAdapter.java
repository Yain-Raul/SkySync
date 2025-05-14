package com.skysync.adapters.in.messaging;

import com.google.gson.Gson;
import com.skysync.application.ports.out.GuardarEventoPort;
import com.skysync.domain.events.FlightEvent;
import com.skysync.domain.events.WeatherEvent;
import com.skysync.infrastructure.config.AppConfig;
import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;

public class EventStoreBrokerAdapter implements MessageListener {

	private final GuardarEventoPort eventStore;
	private final Gson gson = new Gson();

	public EventStoreBrokerAdapter(GuardarEventoPort eventStore) {
		this.eventStore = eventStore;
	}

	public void iniciar() {
		try {
			ConnectionFactory factory = new ActiveMQConnectionFactory(AppConfig.get("BROKER_URL"));
			Connection conn = factory.createConnection();
			conn.setClientID("SkySync-EventStore");
			conn.start();

			Session session = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);
			Topic topicClima = session.createTopic(AppConfig.get("WEATHER_TOPIC"));
			Topic topicVuelos = session.createTopic("prediction.Flight");

			MessageConsumer clima = session.createDurableSubscriber(topicClima, "StoreClima");
			MessageConsumer vuelos = session.createDurableSubscriber(topicVuelos, "StoreVuelos");

			clima.setMessageListener(this);
			vuelos.setMessageListener(this);

			System.out.println("📥 EventStore escuchando eventos...");

		} catch (Exception e) {
			System.out.println("❌ Error iniciando EventStore:");
			e.printStackTrace();
		}
	}

	@Override
	public void onMessage(Message message) {
		if (message instanceof TextMessage text) {
			try {
				String json = text.getText();
				if (json.contains("temperatura") && json.contains("humedad")) {
					WeatherEvent evento = gson.fromJson(json, WeatherEvent.class);
					eventStore.guardarEvento("prediction.Weather", evento.getSs(), evento.getTs(), json);
				} else if (json.contains("numeroVuelo") && json.contains("estado")) {
					FlightEvent evento = gson.fromJson(json, FlightEvent.class);
					eventStore.guardarEvento("prediction.Flight", evento.getSs(), evento.getTs(), json);
				}
			} catch (Exception e) {
				System.out.println("❌ Error procesando evento:");
				e.printStackTrace();
			}
		}
	}
}
