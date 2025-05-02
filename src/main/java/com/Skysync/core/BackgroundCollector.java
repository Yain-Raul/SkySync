package com.Skysync.core;

import com.Skysync.feeders.flights.AviationStackAPI;
import com.Skysync.feeders.weather.OpenWeatherAPI;
import com.Skysync.business.DatamartManager;
import com.Skysync.feeders.weather.WeatherPublisher;
import com.Skysync.models.Clima;
import com.Skysync.models.Vuelo;
import com.Skysync.events.WeatherEvent;
import com.Skysync.feeders.flights.FlightPublisher;
import com.Skysync.events.FlightEvent;


import java.util.List;

public class BackgroundCollector {

	private static final String[] AEROPUERTOS_CANARIOS = {"LPA", "TFN", "TFS", "ACE", "FUE", "GMZ", "SPC", "VDE"};

	public void iniciarModoLento() {
		DatamartManager db = new DatamartManager();
		OpenWeatherAPI apiClima = new OpenWeatherAPI();
		AviationStackAPI apiVuelos = new AviationStackAPI();
		WeatherPublisher publisher = new WeatherPublisher();
		FlightPublisher flightPublisher = new FlightPublisher();

		while (true) {
			System.out.println("\n♻️ Recopilando vuelos y clima en segundo plano...");

			for (String aeropuerto : AEROPUERTOS_CANARIOS) {
				List<Vuelo> vuelos = apiVuelos.obtenerVuelosPorAeropuerto(aeropuerto);
				for (Vuelo vuelo : vuelos) {
					db.insertarVuelo(vuelo);

					// Publicar evento de vuelo
					FlightEvent eventoVuelo = new FlightEvent("feederB", vuelo);
					flightPublisher.publicar(eventoVuelo);
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
