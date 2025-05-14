package com.skysync.adapters.in.scheduled;

import com.skysync.adapters.out.api.AviationStackAdapter;
import com.skysync.adapters.out.api.OpenWeatherClimaAdapter;
import com.skysync.adapters.out.messaging.FlightPublisher;
import com.skysync.adapters.out.messaging.WeatherPublisher;
import com.skysync.domain.model.Clima;
import com.skysync.domain.model.Vuelo;
import com.skysync.domain.events.FlightEvent;
import com.skysync.domain.events.WeatherEvent;

import java.util.List;

public class ScheduledDataCollector {

	private static final String[] AEROPUERTOS_CANARIOS = {
			"LPA", "TFN", "TFS", "ACE", "FUE", "GMZ", "SPC", "VDE"
	};

	private static final String[] CIUDADES_CANARIAS = {
			"Telde", "San Cristobal de la Laguna", "Granadilla de Abona", "Arrecife",
			"Puerto del Rosario", "Santa Cruz de la Palma", "Valverde", "Playa de Santiago"
	};

	private final OpenWeatherClimaAdapter climaApi = new OpenWeatherClimaAdapter();
	private final AviationStackAdapter vuelosApi = new AviationStackAdapter();
	private final WeatherPublisher weatherPublisher = new WeatherPublisher();
	private final FlightPublisher flightPublisher = new FlightPublisher();

	public void iniciarModoLento() {
		while (true) {
			System.out.println("\n♻️ Recolectando vuelos y clima en segundo plano...");

			for (String aeropuerto : AEROPUERTOS_CANARIOS) {
				List<Vuelo> vuelos = vuelosApi.obtenerVuelosPorAeropuerto(aeropuerto);
				for (Vuelo vuelo : vuelos) {
					FlightEvent evento = new FlightEvent("feederB", vuelo);
					flightPublisher.publicar(evento);
				}
			}

			for (String ciudad : CIUDADES_CANARIAS) {
				Clima clima = climaApi.obtenerClima(ciudad);
				if (clima != null) {
					WeatherEvent evento = new WeatherEvent("feederA", clima);
					weatherPublisher.publicar(evento);
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
