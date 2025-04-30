package com.Skysync.api;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.Skysync.models.Clima;
import com.Skysync.main.Config;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import java.util.Map;


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
				JsonObject weather = root.getAsJsonArray("weather").get(0).getAsJsonObject();

				double temperatura = main.get("temp").getAsDouble();
				double humedad = main.get("humidity").getAsDouble();
				double viento = wind.get("speed").getAsDouble();
				String condicion = weather.get("main").getAsString();

				return new Clima(ciudad, temperatura, humedad, viento, condicion);
			} else {
				System.out.println("❌ Error en respuesta clima: " + response.code());
			}
		} catch (Exception e) {
			System.out.println("❗ Excepción en OpenWeather:");
			e.printStackTrace();
		}

		return null;
	}

	public Clima obtenerClimaPorCodigo(String codigoIATA) {
		// Aquí puedes usar un mapa simple para traducir código IATA → ciudad real
		Map<String, String> mapaCiudades = Map.of(
				"LPA", "Las Palmas",
				"TFN", "Santa Cruz de Tenerife",
				"TFS", "Adeje",
				"ACE", "Arrecife",
				"FUE", "Puerto del Rosario"
		);

		String ciudad = mapaCiudades.getOrDefault(codigoIATA.toUpperCase(), null);

		if (ciudad == null) {
			System.out.println("❌ Código IATA no reconocido: " + codigoIATA);
			return null;
		}

		return obtenerClima(ciudad); // método ya existente que pide clima por nombre de ciudad
	}

}
