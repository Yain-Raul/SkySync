package com.Skysync.api;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.Skysync.models.Vuelo;
import com.Skysync.utils.Config;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class AviationStackAPI {
	private static final String API_KEY = Config.AVIATIONSTACK_API_KEY;
	private static final String BASE_URL = "http://api.aviationstack.com/v1/flights?access_key=";
	private final OkHttpClient client = new OkHttpClient();

	public Vuelo obtenerVuelo(String numeroVuelo) {
		String url = BASE_URL + API_KEY + "&flight_iata=" + numeroVuelo;
		System.out.println("📡 Llamando a AviationStack: " + url);

		Request request = new Request.Builder().url(url).build();

		try (Response response = client.newCall(request).execute()) {
			if (response.isSuccessful() && response.body() != null) {
				String json = response.body().string();
				System.out.println("🌐 Respuesta JSON vuelo: " + json);

				JsonObject root = JsonParser.parseString(json).getAsJsonObject();
				JsonArray data = root.getAsJsonArray("data");

				if (data == null || data.size() == 0) {
					System.out.println("⚠️ No se encontraron datos de vuelo");
					return null;
				}

				JsonObject vueloJson = data.get(0).getAsJsonObject();

				String aerolinea = vueloJson.getAsJsonObject("airline").get("name").getAsString();
				String salida = vueloJson.getAsJsonObject("departure").get("airport").getAsString();
				String llegada = vueloJson.getAsJsonObject("arrival").get("airport").getAsString();
				String estado = vueloJson.get("flight_status").getAsString();

				return new Vuelo(numeroVuelo, aerolinea, salida, llegada, estado);
			} else {
				System.out.println("❌ Error en respuesta vuelo: " + response.code());
			}
		} catch (Exception e) {
			System.out.println("❗ Excepción en AviationStack:");
			e.printStackTrace();
		}

		return null;
	}
}
