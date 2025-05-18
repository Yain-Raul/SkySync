package com.skysync.feederweather;

import com.skysync.feederweather.adapters.out.api.OpenWeatherClimaAdapter;
import com.skysync.feederweather.adapters.out.messaging.WeatherPublisher;
import com.skysync.core.domain.model.Clima;
import com.skysync.core.domain.events.WeatherEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScheduledDataCollector {

	private static final Logger logger = LoggerFactory.getLogger(ScheduledDataCollector.class);

	// Array now includes IATA codes: {ciudad, latitud, longitud, iata}
	private static final String[][] CIUDADES_COORDINADAS = {
			{"Telde", "28.0096", "-15.4167", "LPA"},           // Assuming Telde maps to LPA (Las Palmas)
			{"La Laguna", "28.4874", "-16.3159", "TFN"},      // Matches OpenWeatherClimaAdapter
			{"Granadilla", "28.1216", "-16.5769", "TFS"},     // Matches OpenWeatherClimaAdapter
			{"Arrecife", "28.963", "-13.5477", "ACE"},        // Matches OpenWeatherClimaAdapter
			{"Puerto del Rosario", "28.5004", "-13.8625", "FUE"}, // Matches OpenWeatherClimaAdapter
			{"Santa Cruz de la Palma", "28.6836", "-17.7645", "SPC"}, // Matches OpenWeatherClimaAdapter
			{"Valverde", "27.8094", "-17.9158", "VDE"},       // Matches OpenWeatherClimaAdapter
			{"San Sebastián", "28.0901", "-17.1119", "GMZ"}   // Matches OpenWeatherClimaAdapter
	};

	private final OpenWeatherClimaAdapter climaApi = new OpenWeatherClimaAdapter();
	private final WeatherPublisher publisher = new WeatherPublisher();
	private volatile boolean running = true; // Flag for graceful shutdown

	public void iniciarModoLento() {
		// Add shutdown hook for graceful termination
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			logger.info("🛑 Received shutdown signal. Stopping ScheduledDataCollector...");
			running = false;
		}));

		while (running) {
			logger.info("♻️ Recolectando clima...");

			for (String[] ciudadCoord : CIUDADES_COORDINADAS) {
				String ciudad = ciudadCoord[0];
				String lat = ciudadCoord[1];
				String lon = ciudadCoord[2];
				String iata = ciudadCoord[3];

				try {
					Clima clima = climaApi.obtenerClimaPorCoord(ciudad, lat, lon, iata);
					if (clima == null) {
						logger.warn("⚠️ Clima nulo para {}", ciudad);
					} else {
						WeatherEvent evento = new WeatherEvent("feederA", clima);
						publisher.publicar(evento);
						logger.info("📤 Evento de clima publicado: {}", evento);
					}
				} catch (Exception e) {
					logger.error("❌ Error al recolectar clima para {}: {}", ciudad, e.getMessage(), e);
				}
			}

			logger.info("✅ Esperando 1 minuto...");
			try {
				Thread.sleep(60000);
			} catch (InterruptedException e) {
				logger.error("❌ Interrupción durante sleep: {}", e.getMessage(), e);
				Thread.currentThread().interrupt(); // Restore interrupted status
				running = false; // Exit loop on interruption
			}
		}

		logger.info("🛑 ScheduledDataCollector ha finalizado.");
	}

	// Method to stop the collector (for testing or manual shutdown)
	public void stop() {
		logger.info("🛑 Stopping ScheduledDataCollector...");
		running = false;
	}
}