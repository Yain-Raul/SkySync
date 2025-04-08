package com.Skysync.core;

import com.Skysync.api.AviationStackAPI;
import com.Skysync.database.DatabaseManager;
import com.Skysync.models.Vuelo;

import java.util.List;

public class DataCollector {

	private static final String[] AEROPUERTOS_CANARIOS = {"LPA", "TFN", "TFS", "ACE", "FUE"};

	public void recolectarVuelosPorAeropuerto() {
		DatabaseManager db = new DatabaseManager();
		AviationStackAPI api = new AviationStackAPI();

		for (String aeropuerto : AEROPUERTOS_CANARIOS) {
			System.out.println("\n📍 Aeropuerto: " + aeropuerto);
			List<Vuelo> vuelos = api.obtenerVuelosPorAeropuerto(aeropuerto);

			if (vuelos.isEmpty()) {
				System.out.println("⚠️ No se encontraron vuelos.");
			} else {
				for (Vuelo vuelo : vuelos) {
					db.guardarVuelo(vuelo);
				}
				System.out.println("✅ Se guardaron " + vuelos.size() + " vuelos.");
			}
		}
	}
}
