package com.skysync.feederflights.adapters.out.api;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.skysync.core.domain.model.Vuelo;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.util.ArrayList;
import java.util.List;

public class AviationStackAdapter {

	private static final String API_KEY = System.getenv("AVIATIONSTACK_API_KEY");
	private static final String BASE_URL = "http://api.aviationstack.com/v1/flights";
	private final OkHttpClient client = new OkHttpClient();

	public List<Vuelo> obtenerVuelosPorAeropuerto(String iata) {
		List<Vuelo> vuelos = new ArrayList<>();
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

							String numero = vueloJson.getAsJsonObject("flight").get("iata").getAsString();
							String aerolinea = vueloJson.getAsJsonObject("airline").get("name").getAsString();

							JsonObject dep = vueloJson.getAsJsonObject("departure");
							JsonObject arr = vueloJson.getAsJsonObject("arrival");

							String salida = dep.get("airport").getAsString();
							String salidaIATA = dep.get("iata").getAsString();
							String llegada = arr.get("airport").getAsString();
							String estado = vueloJson.get("flight_status").getAsString();

							vuelos.add(new Vuelo(numero, aerolinea, salida, salidaIATA, llegada, estado));
						}
					}
				}
			}
		} catch (Exception e) {
			System.out.println("❌ Error obteniendo vuelos: " + e.getMessage());
		}

		return vuelos;
	}
}
