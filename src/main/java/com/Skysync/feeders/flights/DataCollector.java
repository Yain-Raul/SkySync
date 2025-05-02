package com.Skysync.feeders.flights;

import com.Skysync.feeders.flights.AviationStackAPI;
import com.Skysync.business.DatamartManager; // ✅ Usamos DatamartManager ahora
import com.Skysync.models.Vuelo;

import java.util.List;

public class DataCollector {

	private static final String[] AEROPUERTOS_CANARIOS = {"LPA", "TFN", "TFS", "ACE", "FUE", "GMZ", "SPC", "VDE"};

	public void recolectarVuelosPorAeropuerto() {
		DatamartManager db = new DatamartManager(); // ✅ Cambio aquí
		AviationStackAPI api = new AviationStackAPI();

		for (String aeropuerto : AEROPUERTOS_CANARIOS) {
			System.out.println("\n📍 Aeropuerto: " + aeropuerto);
			List<Vuelo> vuelos = api.obtenerVuelosPorAeropuerto(aeropuerto);

			if (vuelos.isEmpty()) {
				System.out.println("⚠️ No se encontraron vuelos.");
			} else {
				for (Vuelo vuelo : vuelos) {
					db.insertarVuelo(vuelo); // ✅ Cambio aquí
				}
				System.out.println("✅ Se guardaron " + vuelos.size() + " vuelos.");
			}
		}
	}
}
