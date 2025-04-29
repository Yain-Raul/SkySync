package com.Skysync.core;

import com.Skysync.api.AviationStackAPI;
import com.Skysync.api.OpenWeatherAPI;
import com.Skysync.business.DatamartManager;
import com.Skysync.messaging.WeatherPublisher;
import com.Skysync.models.Clima;
import com.Skysync.models.Vuelo;
import com.Skysync.events.WeatherEvent;

import java.util.List;

public class BackgroundCollector {

	private static final String[] AEROPUERTOS_CANARIOS = {"LPA", "TFN", "TFS", "ACE", "FUE", "GMZ", "SPC", "VDE"};

	public void iniciarModoLento() {
		DatamartManager db = new DatamartManager();
		OpenWeatherAPI apiClima = new OpenWeatherAPI();
		AviationStackAPI apiVuelos = new AviationStackAPI();
		WeatherPublisher publisher = new WeatherPublisher();

		while (true) {
			System.out.println("\n♻️ Recopilando vuelos y clima en segundo plano...");

			// Recolectar vuelos
			for (String aeropuerto : AEROPUERTOS_CANARIOS) {
					List<Vuelo> vuelos = apiVuelos.obtenerVuelosPorAeropuerto(aeropuerto);
				for (Vuelo vuelo : vuelos) {
					db.insertarVuelo(vuelo);
				}
			}

			// Recolectar clima
			String[] ciudades = {"Telde",
					"San Cristobal de la Laguna",
					"Granadilla de Abona",
					"Arrecife",
					"Puerto del Rosario",
					"Santa Cruz de la Palma",
					"Valverde",
					"Playa de Santiago"};
			for (String ciudad : ciudades) {
				Clima clima = apiClima.obtenerClima(ciudad);
				if (clima != null) {
					db.insertarClima(clima);

					// Publicar evento de clima
					WeatherEvent evento = new WeatherEvent("feederA", clima);
					publisher.publicar(evento);
				}
			}

			System.out.println("✅ Recolección terminada. Esperando 1 minuto...");

			try {
				Thread.sleep(60000); // Esperar 1 minuto
			} catch (InterruptedException e) {
				System.out.println("❌ Error en espera:");
				e.printStackTrace();
			}
		}
	}
}
