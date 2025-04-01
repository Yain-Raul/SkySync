package com.Skysync;

import com.Skysync.api.AviationStackAPI;
import com.Skysync.api.OpenWeatherAPI;
import com.Skysync.database.DatabaseManager;
import com.Skysync.models.Clima;
import com.Skysync.models.Vuelo;

public class Main {
	public static void main(String[] args) {
		// Inicializar base de datos y crear tablas si no existen
		DatabaseManager db = new DatabaseManager();
		db.crearTablas();

		// Crear instancias de las APIs
		OpenWeatherAPI climaAPI = new OpenWeatherAPI();
		AviationStackAPI vueloAPI = new AviationStackAPI();

		// Obtener datos desde APIs
		Clima clima = climaAPI.obtenerClima("Madrid");
		Vuelo vuelo = vueloAPI.obtenerVuelo("LH123");

		// Mostrar datos por consola
		if (clima != null) System.out.println("🌤️ Clima: " + clima);
		if (vuelo != null) System.out.println("✈️ Vuelo: " + vuelo);

		// Guardar en base de datos
		if (clima != null) db.guardarClima(clima);
		if (vuelo != null) db.guardarVuelo(vuelo);
	}
}
