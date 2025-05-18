package com.skysync.feederflights;

import com.skysync.feederflights.adapters.out.api.AviationStackAdapter;
import com.skysync.feederflights.adapters.out.messaging.FlightPublisher;
import com.skysync.core.domain.model.Vuelo;
import com.skysync.core.domain.events.FlightEvent;

import javax.jms.JMSException;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ScheduledDataCollector {

	private static final String[] AEROPUERTOS_CANARIOS = {
			"LPA", "TFN", "TFS", "ACE", "FUE", "GMZ", "SPC", "VDE"
	};

	private final AviationStackAdapter vuelosApi = new AviationStackAdapter();
	private final FlightPublisher publisher;
	private final Set<String> publishedFlightIds = new HashSet<>(); // For deduplication
	private LocalDate lastCleanupDate = LocalDate.now();

	public ScheduledDataCollector() throws JMSException {
		this.publisher = new FlightPublisher();
	}

	public void iniciarModoLento() {
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			try {
				publisher.close();
				System.out.println("🛑 FlightPublisher closed.");
			} catch (JMSException e) {
				System.out.println("❌ Error closing FlightPublisher: " + e.getMessage());
			}
		}));

		while (true) {
			// Cleanup publishedFlightIds daily to prevent memory growth
			if (!LocalDate.now().equals(lastCleanupDate)) {
				publishedFlightIds.clear();
				lastCleanupDate = LocalDate.now();
				System.out.println("🧹 Cleared published flight IDs for new day.");
			}

			System.out.println("\n♻️ Recolectando vuelos desde AviationStack...");

			for (String aeropuerto : AEROPUERTOS_CANARIOS) {
				List<Vuelo> vuelos = vuelosApi.obtenerVuelosPorAeropuerto(aeropuerto);
				for (Vuelo vuelo : vuelos) {
					// More robust deduplication key
					String flightId = String.format("%s-%s-%s-%s",
							vuelo.getNumeroVuelo(),
							vuelo.getFecha(),
							vuelo.getAeropuertoSalidaIATA(),
							vuelo.getAeropuertoLlegada());
					if (!publishedFlightIds.contains(flightId)) { // Deduplicate
						publishedFlightIds.add(flightId);
						FlightEvent evento = new FlightEvent("feederB", vuelo);
						try {
							publisher.publicar(evento);
							System.out.println("📤 Evento de vuelo publicado: " + evento);
						} catch (JMSException e) {
							System.out.println("❌ Error publicando evento para vuelo " + vuelo.getNumeroVuelo() + ": " + e.getMessage());
							e.printStackTrace();
						}
					}
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

	public static void main(String[] args) {
		try {
			ScheduledDataCollector collector = new ScheduledDataCollector();
			collector.iniciarModoLento();
		} catch (JMSException e) {
			System.out.println("❌ Error initializing ScheduledDataCollector: " + e.getMessage());
			System.exit(1);
		}
	}
}