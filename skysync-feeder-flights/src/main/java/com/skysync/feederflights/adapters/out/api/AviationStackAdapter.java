package com.skysync.feederflights.adapters.out.api;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.skysync.core.domain.model.Vuelo;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public class AviationStackAdapter {

	private static final String API_KEY = System.getenv("AVIATIONSTACK_API_KEY");
	private static final String BASE_URL = "http://api.aviationstack.com/v1/flights";
	private final OkHttpClient client = new OkHttpClient();

	public List<Vuelo> obtenerVuelosPorAeropuerto(String iata) {
		List<Vuelo> vuelos = new ArrayList<>();
		if (iata == null || !iata.matches("[A-Z]{3}")) {
			System.out.println("❌ Invalid IATA code: " + iata);
			return vuelos;
		}

		String url = BASE_URL + "?access_key=" + API_KEY + "&dep_iata=" + iata;

		try {
			Request request = new Request.Builder().url(url).build();
			try (Response response = client.newCall(request).execute()) {
				if (response.isSuccessful() && response.body() != null) {
					String json = response.body().string();
					JsonObject root = JsonParser.parseString(json).getAsJsonObject();
					JsonArray data = root.getAsJsonArray("data");

					if (data != null) {
						for (var element : data) {
							JsonObject vueloJson = element.getAsJsonObject();

							// Safely extract fields with null checks
							String numero = getJsonString(vueloJson.getAsJsonObject("flight"), "iata");
							String aerolinea = getJsonString(vueloJson.getAsJsonObject("airline"), "name");

							JsonObject dep = vueloJson.getAsJsonObject("departure");
							JsonObject arr = vueloJson.getAsJsonObject("arrival");

							String salida = getJsonString(dep, "airport");
							String salidaIATA = getJsonString(dep, "iata");
							String llegada = getJsonString(arr, "airport");
							String estado = getJsonString(vueloJson, "flight_status");

							// Validate estado
							if (estado != null) {
								estado = normalizeFlightStatus(estado);
							}

							// Extract and validate fecha
							String fecha = null;
							if (dep != null && !dep.get("scheduled").isJsonNull()) {
								String scheduled = dep.get("scheduled").getAsString(); // e.g., "2025-05-18T14:30:00+00:00"
								try {
									ZonedDateTime dateTime = ZonedDateTime.parse(scheduled);
									fecha = dateTime.toLocalDate().toString(); // e.g., "2025-05-18"
								} catch (DateTimeParseException e) {
									System.out.println("❌ Invalid scheduled date format for flight " + numero + ": " + scheduled);
									continue; // Skip this vuelo
								}
							}

							// Extract delay reason if available
							String razonRetraso = null;
							if (estado != null && estado.toLowerCase().contains("delay") && dep != null && !dep.get("delay").isJsonNull()) {
								int delayMinutes = dep.get("delay").getAsInt();
								razonRetraso = "Delayed by " + delayMinutes + " minutes";
							}

							// Only add the vuelo if all required fields are present and valid
							if (numero != null && aerolinea != null && salida != null && salidaIATA != null &&
									llegada != null && estado != null && fecha != null) {
								Vuelo vuelo = new Vuelo(numero, aerolinea, salida, salidaIATA, llegada, estado, fecha, razonRetraso);
								vuelos.add(vuelo);
							} else {
								System.out.println("❌ Skipping vuelo due to missing or invalid fields: " + vueloJson);
							}
						}
					}
				} else {
					System.out.println("❌ API request failed with status: " + response.code());
				}
			}
		} catch (Exception e) {
			System.out.println("❌ Error obteniendo vuelos: " + e.getMessage());
			e.printStackTrace();
		}

		return vuelos;
	}

	// Helper method to safely extract string values from JSON
	private String getJsonString(JsonObject json, String key) {
		if (json != null && json.has(key) && !json.get(key).isJsonNull()) {
			return json.get(key).getAsString();
		}
		return null;
	}

	// Normalize flight status to consistent values
	private String normalizeFlightStatus(String status) {
		if (status == null) return "unknown";
		status = status.toLowerCase();
		if (status.contains("schedul")) return "scheduled";
		if (status.contains("activ")) return "active";
		if (status.contains("land")) return "landed";
		if (status.contains("cancel")) return "cancelled";
		if (status.contains("delay")) return "delayed";
		return status;
	}
}