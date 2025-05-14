package com.skysync.adapters.out.api;

import com.skysync.application.ports.out.ClimaPorCodigoPort;
import com.skysync.domain.model.Clima;
import com.skysync.infrastructure.config.AppConfig;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.Map;

public class OpenWeatherClimaAdapter implements ClimaPorCodigoPort {

	private static final String API_KEY = AppConfig.get("OPENWEATHER_API_KEY");
	private static final String BASE_URL = "https://api.openweathermap.org/data/2.5/weather?q=";
	private final OkHttpClient client = new OkHttpClient();

	private static final Map<String, String> IATA_TO_CIUDAD = Map.of(
			"LPA", "Las Palmas",
			"TFN", "Santa Cruz de Tenerife",
			"TFS", "Adeje",
			"ACE", "Arrecife",
			"FUE", "Puerto del Rosario",
			"SPC", "Santa Cruz de La Palma",
			"GMZ", "San Sebastián de La Gomera",
			"VDE", "Valverde"
	);

	@Override
	public Clima obtenerClimaPorCodigo(String codigo) {
		String ciudad = IATA_TO_CIUDAD.getOrDefault(codigo.toUpperCase(), null);
		if (ciudad == null) return null;

		String url = BASE_URL + ciudad + "&appid=" + API_KEY + "&units=metric";
		Request request = new Request.Builder().url(url).build();

		try (Response response = client.newCall(request).execute()) {
			if (response.isSuccessful() && response.body() != null) {
				String json = response.body().string();
				JsonObject root = JsonParser.parseString(json).getAsJsonObject();

				double temperatura = root.getAsJsonObject("main").get("temp").getAsDouble();
				double humedad = root.getAsJsonObject("main").get("humidity").getAsDouble();
				double viento = root.getAsJsonObject("wind").get("speed").getAsDouble();
				String condicion = root.getAsJsonArray("weather").get(0).getAsJsonObject().get("main").getAsString();

				return new Clima(ciudad, temperatura, humedad, viento, condicion);
			}
		} catch (Exception e) {
			System.out.println("❌ Error obteniendo clima: " + e.getMessage());
		}
		return null;
	}

	public Clima obtenerClima(String ciudad) {
		String url = BASE_URL + ciudad + "&appid=" + API_KEY + "&units=metric";
		Request request = new Request.Builder().url(url).build();

		try (Response response = client.newCall(request).execute()) {
			if (response.isSuccessful() && response.body() != null) {
				String json = response.body().string();
				JsonObject root = JsonParser.parseString(json).getAsJsonObject();

				double temperatura = root.getAsJsonObject("main").get("temp").getAsDouble();
				double humedad = root.getAsJsonObject("main").get("humidity").getAsDouble();
				double viento = root.getAsJsonObject("wind").get("speed").getAsDouble();
				String condicion = root.getAsJsonArray("weather").get(0).getAsJsonObject().get("main").getAsString();

				return new Clima(ciudad, temperatura, humedad, viento, condicion);
			}
		} catch (Exception e) {
			System.out.println("❌ Error obteniendo clima para ciudad: " + ciudad);
			e.printStackTrace();
		}
		return null;
	}
}

