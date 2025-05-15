package com.skysync.feederweather.adapters.out.api;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.skysync.core.aplication.ports.out.ClimaPorCodigoPort;
import com.skysync.core.domain.model.Clima;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import java.util.Map;

public class OpenWeatherClimaAdapter implements ClimaPorCodigoPort {

	private static final String API_KEY = System.getenv("OPENWEATHER_API_KEY");
	private final OkHttpClient client = new OkHttpClient();

	public Clima obtenerClimaPorCoord(String ciudad, String lat, String lon) {
		String url = "https://api.openweathermap.org/data/2.5/weather?lat=" + lat +
				"&lon=" + lon + "&appid=" + API_KEY + "&units=metric";
		System.out.println("🔑 Usando API Key: " + API_KEY);
		Request request = new Request.Builder().url(url).build();

		try (Response response = client.newCall(request).execute()) {
			String json = response.body() != null ? response.body().string() : "❌ Sin contenido";
			System.out.println("📦 Respuesta JSON de OpenWeather para " + ciudad + ": " + json);

			if (response.isSuccessful() && response.body() != null) {
				JsonObject root = JsonParser.parseString(json).getAsJsonObject();

				double temperatura = root.getAsJsonObject("main").get("temp").getAsDouble();
				double humedad = root.getAsJsonObject("main").get("humidity").getAsDouble();
				double viento = root.getAsJsonObject("wind").get("speed").getAsDouble();
				String condicion = root.getAsJsonArray("weather").get(0).getAsJsonObject().get("main").getAsString();

				return new Clima(ciudad, temperatura, humedad, viento, condicion);
			}
		} catch (Exception e) {
			System.out.println("❌ Error obteniendo clima de " + ciudad + ": " + e.getMessage());
		}

		return null;
	}
	@Override
	public Clima obtenerClimaPorCodigo(String codigo) {
		// Mapa IATA → {ciudad, latitud, longitud}
		Map<String, String[]> mapa = Map.of(
				"LPA", new String[]{"Las Palmas", "28.0096", "-15.4167"},
				"TFN", new String[]{"La Laguna", "28.4874", "-16.3159"},
				"TFS", new String[]{"Granadilla", "28.1216", "-16.5769"},
				"ACE", new String[]{"Arrecife", "28.963", "-13.5477"},
				"FUE", new String[]{"Puerto del Rosario", "28.5004", "-13.8625"},
				"SPC", new String[]{"Santa Cruz de la Palma", "28.6836", "-17.7645"},
				"GMZ", new String[]{"San Sebastián", "28.0901", "-17.1119"},
				"VDE", new String[]{"Valverde", "27.8094", "-17.9158"}
		);

		if (!mapa.containsKey(codigo)) {
			System.out.println("❌ Código IATA no reconocido: " + codigo);
			return null;
		}

		String[] datos = mapa.get(codigo);
		return obtenerClimaPorCoord(datos[0], datos[1], datos[2]);
	}

}
