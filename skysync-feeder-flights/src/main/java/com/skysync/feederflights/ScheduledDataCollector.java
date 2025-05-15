package com.skysync.feederflights;

import com.skysync.feederflights.adapters.out.api.AviationStackAdapter;
import com.skysync.feederflights.adapters.out.messaging.FlightPublisher;
import com.skysync.core.domain.model.Vuelo;
import com.skysync.core.domain.events.FlightEvent;

import java.util.List;

public class ScheduledDataCollector {

	private static final String[] AEROPUERTOS_CANARIOS = {
			"LPA", "TFN", "TFS", "ACE", "FUE", "GMZ", "SPC", "VDE"
	};

	private final AviationStackAdapter vuelosApi = new AviationStackAdapter();
	private final FlightPublisher publisher = new FlightPublisher();

	public void iniciarModoLento() {
		while (true) {
			System.out.println("\n♻️ Recolectando vuelos desde AviationStack...");

			for (String aeropuerto : AEROPUERTOS_CANARIOS) {
				List<Vuelo> vuelos = vuelosApi.obtenerVuelosPorAeropuerto(aeropuerto);
				for (Vuelo vuelo : vuelos) {
					FlightEvent evento = new FlightEvent("feederB", vuelo);
					publisher.publicar(evento);
					System.out.println("📤 Evento de vuelo publicado: " + evento);

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
