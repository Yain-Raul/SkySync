package com.skysync.feederweather;

import com.skysync.feederweather.adapters.out.api.OpenWeatherClimaAdapter;
import com.skysync.feederweather.adapters.out.messaging.WeatherPublisher;
import com.skysync.core.domain.model.Clima;
import com.skysync.core.domain.events.WeatherEvent;

public class ScheduledDataCollector {

	private static final String[][] CIUDADES_COORDENADAS = {
			{"Telde", "28.0096", "-15.4167"},
			{"La Laguna", "28.4874", "-16.3159"},
			{"Granadilla", "28.1216", "-16.5769"},
			{"Arrecife", "28.963", "-13.5477"},
			{"Puerto del Rosario", "28.5004", "-13.8625"},
			{"Santa Cruz de la Palma", "28.6836", "-17.7645"},
			{"Valverde", "27.8094", "-17.9158"},
			{"San Sebastián", "28.0901", "-17.1119"}
	};


	private final OpenWeatherClimaAdapter climaApi = new OpenWeatherClimaAdapter();
	private final WeatherPublisher publisher = new WeatherPublisher();

	public void iniciarModoLento() {
		while (true) {
			System.out.println("\n♻️ Recolectando clima...");

			for (String[] ciudadCoord : CIUDADES_COORDENADAS) {
				String ciudad = ciudadCoord[0];
				String lat = ciudadCoord[1];
				String lon = ciudadCoord[2];

				Clima clima = climaApi.obtenerClimaPorCoord(ciudad, lat, lon);

				if (clima == null) {
					System.out.println("⚠️ Clima nulo para " + ciudad);
				} else {
					WeatherEvent evento = new WeatherEvent("feederA", clima);
					publisher.publicar(evento);
					System.out.println("📤 Evento de clima publicado: " + evento);
				}
			}

			System.out.println("✅ Esperando 1 minuto...");
			try {
				Thread.sleep(60000);
			} catch (InterruptedException e) {
				System.out.println("❌ Interrupción:");
				e.printStackTrace();
			}
		}
	}
}
