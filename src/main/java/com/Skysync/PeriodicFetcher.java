package com.Skysync;

import com.Skysync.api.AviationStackAPI;
import com.Skysync.api.OpenWeatherAPI;
import com.Skysync.database.DatabaseManager;
import com.Skysync.models.Clima;
import com.Skysync.models.Vuelo;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class PeriodicFetcher {

	public static void main(String[] args) {
		DatabaseManager db = new DatabaseManager();
		db.crearTablas(); // Por si acaso

		ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

		Runnable tarea = () -> {
			System.out.println("\n⏱️ Ejecutando tarea de captura de datos...");

			OpenWeatherAPI climaAPI = new OpenWeatherAPI();
			AviationStackAPI vueloAPI = new AviationStackAPI();

			Clima clima = climaAPI.obtenerClima("Madrid");
			Vuelo vuelo = vueloAPI.obtenerVuelo("LH123"); // puedes cambiar este código

			if (clima != null) {
				db.guardarClima(clima);
				System.out.println("✅ Clima guardado: " + clima);
			} else {
				System.out.println("⚠️ No se pudo obtener clima.");
			}

			if (vuelo != null) {
				db.guardarVuelo(vuelo);
				System.out.println("✅ Vuelo guardado: " + vuelo);
			} else {
				System.out.println("⚠️ No se pudo obtener vuelo.");
			}
		};

		// Ejecutar ahora y luego cada 3600 segundos (1 hora)
		scheduler.scheduleAtFixedRate(tarea, 0, 3600, TimeUnit.SECONDS);
	}
}
