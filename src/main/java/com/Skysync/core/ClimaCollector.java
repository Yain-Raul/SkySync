package com.Skysync.core;

import com.Skysync.api.OpenWeatherAPI;
import com.Skysync.business.DatamartManager;
import com.Skysync.models.Clima;
import com.Skysync.events.WeatherEvent;
import com.Skysync.messaging.WeatherPublisher;

public class ClimaCollector {

	private static final String[] CIUDADES = {
			"Las Palmas",
			"Santa Cruz de Tenerife",
			"Adeje",
			"Arrecife",
			"Puerto del Rosario"
	};

	public void recolectarClimaActual() {
		OpenWeatherAPI api = new OpenWeatherAPI();
		DatamartManager db = new DatamartManager(); // ✅ Cambio aquí
		WeatherPublisher publisher = new WeatherPublisher();

		for (String ciudad : CIUDADES) {
			Clima clima = api.obtenerClima(ciudad);
			if (clima != null) {
				db.insertarClima(clima); // ✅ Cambio aquí
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
