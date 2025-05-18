package com.skysync.feederweather.adapters.out.api;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.skysync.core.aplication.ports.out.ClimaPorCodigoPort;
import com.skysync.core.domain.model.Clima;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class OpenWeatherClimaAdapter implements ClimaPorCodigoPort {

	private static final Logger logger = LoggerFactory.getLogger(OpenWeatherClimaAdapter.class);
	private static final String API_KEY = System.getenv("OPENWEATHER_API_KEY");
	private static final OkHttpClient client = new OkHttpClient(); // Reused instance

	// Mapa IATA → {ciudad, latitud, longitud}
	private static final Map<String, String[]> IATA_COORDINATES = Map.of(
			"LPA", new String[]{"Las Palmas", "28.0096", "-15.4167"},
			"TFN", new String[]{"La Laguna", "28.4874", "-16.3159"},
			"TFS", new String[]{"Granadilla", "28.1216", "-16.5769"},
			"ACE", new String[]{"Arrecife", "28.963", "-13.5477"},
			"FUE", new String[]{"Puerto del Rosario", "28.5004", "-13.8625"},
			"SPC", new String[]{"Santa Cruz de la Palma", "28.6836", "-17.7645"},
			"GMZ", new String[]{"San Sebastián", "28.0901", "-17.1119"},
			"VDE", new String[]{"Valverde", "27.8094", "-17.9158"}
	);

	public Clima obtenerClimaPorCoord(String ciudad, String lat, String lon, String airportCode) {
		if (API_KEY == null) {
			logger.error("OPENWEATHER_API_KEY environment variable not set");
			throw new RuntimeException("OPENWEATHER_API_KEY environment variable not set");
		}

		String url = "https://api.openweathermap.org/data/2.5/weather?lat=" + lat +
				"&lon=" + lon + "&appid=" + API_KEY + "&units=metric";
		logger.debug("🔑 Usando API Key para URL: {}", url);
		Request request = new Request.Builder().url(url).build();

		try (Response response = client.newCall(request).execute()) {
			if (response.body() == null) {
				logger.error("❌ Respuesta vacía de OpenWeather para {}: Sin contenido", ciudad);
				throw new RuntimeException("Respuesta vacía de OpenWeather para " + ciudad);
			}

			String json = response.body().string();
			logger.debug("📦 Respuesta JSON de OpenWeather para {}: {}", ciudad, json);

			if (response.isSuccessful()) {
				JsonObject root = JsonParser.parseString(json).getAsJsonObject();

				double temperatura = root.getAsJsonObject("main").get("temp").getAsDouble();
				double humedad = root.getAsJsonObject("main").get("humidity").getAsDouble();
				double viento = root.getAsJsonObject("wind").get("speed").getAsDouble();
				String condicion = root.getAsJsonArray("weather").get(0).getAsJsonObject().get("main").getAsString();

				return new Clima(ciudad, airportCode, temperatura, humedad, viento, condicion);
			} else {
				logger.error("❌ Error en la solicitud a OpenWeather para {}: Código {}", ciudad, response.code());
				throw new RuntimeException("Error en la solicitud a OpenWeather para " + ciudad + ": Código " + response.code());
			}
		} catch (Exception e) {
			logger.error("❌ Error obteniendo clima de {}: {}", ciudad, e.getMessage(), e);
			throw new RuntimeException("Error obteniendo clima de " + ciudad + ": " + e.getMessage(), e);
		}
	}

	@Override
	public Clima obtenerClimaPorCodigo(String codigo) {
		if (codigo == null || codigo.isBlank()) {
			logger.error("❌ Código IATA no especificado");
			throw new IllegalArgumentException("Código IATA no especificado");
		}

		if (!IATA_COORDINATES.containsKey(codigo.toUpperCase())) {
			logger.error("❌ Código IATA no reconocido: {}", codigo);
			throw new IllegalArgumentException("Código IATA no reconocido: " + codigo);
		}

		String[] datos = IATA_COORDINATES.get(codigo.toUpperCase());
		return obtenerClimaPorCoord(datos[0], datos[1], datos[2], codigo); // Pass codigo as airportCode
	}
}