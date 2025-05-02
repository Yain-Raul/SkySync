package com.Skysync.feeders.flights;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.Skysync.models.Vuelo;
import com.Skysync.config.AppConfig;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.util.ArrayList;
import java.util.List;

public class AviationStackAPI {

	private static final String BASE_URL = "http://api.aviationstack.com/v1/flights";

	String API_KEY = AppConfig.get("AVIATIONSTACK_API_KEY");
	private final OkHttpClient client = new OkHttpClient();


	public List<Vuelo> obtenerVuelosPorAeropuerto(String aeropuertoIATA) {
		List<Vuelo> lista = new ArrayList<>();

		String url = BASE_URL + "?access_key=" + API_KEY + "&dep_iata=" + aeropuertoIATA;

		System.out.println("📡 Llamando a AviationStack: " + url);

		Request request = new Request.Builder().url(url).build();

		try (Response response = client.newCall(request).execute()) {
			if (response.isSuccessful() && response.body() != null) {
				String json = response.body().string();
				System.out.println("🌐 Respuesta JSON:\n" + json);

				JsonObject root = JsonParser.parseString(json).getAsJsonObject();
				JsonArray data = root.getAsJsonArray("data");

				if (data == null || data.size() == 0) return lista;

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

					lista.add(new Vuelo(numero, aerolinea, salida, salidaIATA, llegada, estado));
				}
			} else {
				System.out.println("❌ Error HTTP: " + response.code());
			}
		} catch (Exception e) {
			System.out.println("❌ Error: " + e.getMessage());
		}

		return lista;
	}
}
