package com.Skysync.core;

import com.Skysync.api.OpenWeatherAPI;
import com.Skysync.business.DatamartManager;
import com.Skysync.models.Clima;
import com.Skysync.events.WeatherEvent;
import com.Skysync.messaging.WeatherPublisher;

public class ClimaCollector {

	private static final String[] CIUDADES = {
			"Telde",
			"San Cristobal de la Laguna",
			"Granadilla de Abona",
			"Arrecife",
			"Puerto del Rosario",
			"Santa Cruz de la Palma",
			"Valverde",
			"Playa de Santiago"
	};

	public void recolectarClimaActual() {
		OpenWeatherAPI api = new OpenWeatherAPI();
		DatamartManager db = new DatamartManager();
		WeatherPublisher publisher = new WeatherPublisher();

		for (String ciudad : CIUDADES) {
			Clima clima = api.obtenerClima(ciudad);
			if (clima != null) {
				db.insertarClima(clima); //
				System.out.println("✅ Clima guardado: " + clima);

				// Publicar evento en ActiveMQ
				WeatherEvent evento = new WeatherEvent("feederA", clima);
				publisher.publicar(evento);
			} else {
				System.out.println("⚠️ Fallo obteniendo clima de " + ciudad);
			}
		}
	}
}
