package com.Skysync.core;

import com.Skysync.api.OpenWeatherAPI;
import com.Skysync.database.DatabaseManager;
import com.Skysync.models.Clima;

public class ClimaCollector {

	private static final String[] CIUDADES = {"Las Palmas", "Santa Cruz de Tenerife", "Adeje", "Arrecife", "Puerto del Rosario"};

	public void recolectarClimaActual() {
		OpenWeatherAPI api = new OpenWeatherAPI();
		DatabaseManager db = new DatabaseManager();

		for (String ciudad : CIUDADES) {
			Clima clima = api.obtenerClima(ciudad);
			if (clima != null) {
				db.guardarClima(clima);
				System.out.println("✅ Clima guardado: " + clima);
			} else {
				System.out.println("⚠️ Fallo obteniendo clima de " + ciudad);
			}
		}
	}
}
