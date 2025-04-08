package com.Skysync.core;

import com.Skysync.api.AviationStackAPI;
import com.Skysync.database.DatabaseManager;
import com.Skysync.models.Vuelo;

import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.List;

public class BackgroundCollector {

	private static final String[] AEROPUERTOS_CANARIOS = {"LPA", "TFN", "TFS", "ACE", "FUE"};
	private static final Random rand = new Random();

	public void iniciarModoLento() {
		DatabaseManager db = new DatabaseManager();
		AviationStackAPI api = new AviationStackAPI();

		ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

		Runnable tarea = () -> {
			String aeropuerto = AEROPUERTOS_CANARIOS[rand.nextInt(AEROPUERTOS_CANARIOS.length)];
			System.out.println("⏳ Aeropuerto aleatorio: " + aeropuerto);

			List<Vuelo> vuelos = api.obtenerVuelosPorAeropuerto(aeropuerto);
			if (vuelos.isEmpty()) {
				System.out.println("⚠️ Sin resultados para " + aeropuerto);
			} else {
				for (Vuelo vuelo : vuelos) {
					db.guardarVuelo(vuelo);
				}
				System.out.println("✅ " + vuelos.size() + " vuelos guardados.");
			}
		};

		System.out.println("🌀 Recolección en segundo plano activa (1 min por aeropuerto)");
		scheduler.scheduleAtFixedRate(tarea, 0, 60, TimeUnit.SECONDS);
	}
}
