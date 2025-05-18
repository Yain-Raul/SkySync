package com.skysync.adapters.in.messaging;

import com.google.gson.Gson;
import com.skysync.core.aplication.ports.in.EventoClimaListener;
import com.skysync.core.aplication.ports.in.EventoVueloListener;
import com.skysync.core.domain.events.FlightEvent;
import com.skysync.core.domain.events.WeatherEvent;
import com.skysync.infrastructure.config.AppConfig;
import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;

public class BusinessUnitEventAdapter implements MessageListener {

	private final EventoClimaListener climaListener;
	private final EventoVueloListener vueloListener;

	private volatile boolean running = false;
	private final Gson gson = new Gson();

	public BusinessUnitEventAdapter(EventoClimaListener climaListener, EventoVueloListener vueloListener) {
		this.climaListener = climaListener;
		this.vueloListener = vueloListener;
	}

	public void iniciar() {
		try {
			ConnectionFactory factory = new ActiveMQConnectionFactory(AppConfig.get("BROKER_URL"));
			Connection connection = factory.createConnection();
			connection.setClientID("SkySync-BusinessUnit");
			connection.start();

			Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

			Topic weatherTopic = session.createTopic(AppConfig.get("WEATHER_TOPIC"));
			Topic flightTopic = session.createTopic("prediction.Flight");

			MessageConsumer consumerWeather = session.createDurableSubscriber(weatherTopic, "DurableBusinessUnitWeather");
			MessageConsumer consumerFlight = session.createDurableSubscriber(flightTopic, "DurableBusinessUnitFlight");

			consumerWeather.setMessageListener(this);
			consumerFlight.setMessageListener(this);

			System.out.println("📡 BusinessUnit escuchando eventos...");
		} catch (Exception e) {
			System.out.println("❌ Error iniciando BusinessUnit:");
			e.printStackTrace();
		}
	}

	@Override
	public void onMessage(Message message) {
		if (message instanceof TextMessage textMessage) {
			try {
				String json = textMessage.getText();
				if (json.contains("temperatura") && json.contains("humedad")) {
					WeatherEvent evento = gson.fromJson(json, WeatherEvent.class);
					climaListener.procesar(evento);
				} else if (json.contains("numeroVuelo") && json.contains("estado")) {
					FlightEvent evento = gson.fromJson(json, FlightEvent.class);
					vueloListener.procesar(evento);
				} else {
					System.out.println("⚠️ Evento no reconocido.");
				}
			} catch (Exception e) {
				System.out.println("❌ Error procesando evento:");
				e.printStackTrace();
			}
		}
	}
	public boolean isRunning() {
		return running;
	}

	public void stop() {
		running = false;
	}
}
