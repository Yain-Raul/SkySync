package com.skysync.feederflights;

import com.skysync.adapters.out.persistence.SQLiteVueloRepository;
import com.skysync.core.aplication.ports.out.VueloRepository;
import com.skysync.feederflights.adapters.out.api.AviationStackAdapter;
import com.skysync.core.domain.model.Vuelo;
import com.skysync.core.domain.events.FlightEvent;
import com.skysync.feederflights.adapters.out.messaging.FlightPublisher;

import javax.jms.JMSException;
import java.util.List;

public class FlightFeeder {

	private final AviationStackAdapter aviationStackAdapter;
	private final VueloRepository vueloRepository;
	private final FlightPublisher flightPublisher;

	public FlightFeeder() throws JMSException {
		this.aviationStackAdapter = new AviationStackAdapter();
		this.vueloRepository = new SQLiteVueloRepository();
		this.flightPublisher = new FlightPublisher();
	}

	public void fetchAndSaveFlights(String iata) {
		System.out.println("🌍 Fetching flights for IATA: " + iata);
		List<Vuelo> vuelos = aviationStackAdapter.obtenerVuelosPorAeropuerto(iata);
		if (vuelos.isEmpty()) {
			System.out.println("⚠️ No flights found for IATA: " + iata);
			return;
		}

		for (Vuelo vuelo : vuelos) {
			vueloRepository.guardar(vuelo); // Save to database
			FlightEvent event = new FlightEvent("feederA", vuelo); // Correct constructor
			try {
				flightPublisher.publicar(event); // Publish event
			} catch (JMSException e) {
				System.out.println("❌ Failed to publish event for flight " + vuelo.getNumeroVuelo() + ": " + e.getMessage());
			}
		}
		System.out.println("✅ Saved and published " + vuelos.size() + " flights to the datamart and topic.");
	}

	public static void main(String[] args) throws JMSException {
		FlightFeeder feeder = new FlightFeeder();
		if (args.length > 0) {
			feeder.fetchAndSaveFlights(args[0]); // Allow IATA as command-line argument
		} else {
			feeder.fetchAndSaveFlights("MAD"); // Default to Madrid
		}
	}
}