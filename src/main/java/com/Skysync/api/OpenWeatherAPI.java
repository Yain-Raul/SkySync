package com.Skysync.api;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.Skysync.models.Clima;
import com.Skysync.main.Config;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class OpenWeatherAPI {
	private static final String API_KEY = Config.OPENWEATHER_API_KEY;
	private static final String BASE_URL = "https://api.openweathermap.org/data/2.5/weather?q=";
	private final OkHttpClient client = new OkHttpClient();

	public Clima obtenerClima(String ciudad) {
		String url = BASE_URL + ciudad + "&appid=" + API_KEY + "&units=metric";
		System.out.println("📡 Llamando a OpenWeather: " + url);

		Request request = new Request.Builder().url(url).build();

		try (Response response = client.newCall(request).execute()) {
			if (response.isSuccessful() && response.body() != null) {
				String json = response.body().string();
				System.out.println("🌐 Respuesta JSON clima: " + json);

				JsonObject root = JsonParser.parseString(json).getAsJsonObject();
				JsonObject main = root.getAsJsonObject("main");
				JsonObject wind = root.getAsJsonObject("wind");

				double temperatura = main.get("temp").getAsDouble();
				double humedad = main.get("humidity").getAsDouble();
				double viento = wind.get("speed").getAsDouble();

				return new Clima(ciudad, temperatura, humedad, viento);
			} else {
				System.out.println("❌ Error en respuesta clima: " + response.code());
			}
		} catch (Exception e) {
			System.out.println("❗ Excepción en OpenWeather:");
			e.printStackTrace();
		}

		return null;
	}
}
