package com.skysync.application.services;


import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.skysync.core.domain.model.DisruptionForecast;
import com.skysync.core.aplication.ports.out.DisruptionForecastPort;
import com.skysync.utils.EventStoreReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class DisruptionForecastService implements DisruptionForecastPort {
	private static final Logger logger = LoggerFactory.getLogger(DisruptionForecastService.class);
	private final String openWeatherApiKey;
	private final String eventStorePath;
	private final Gson gson;

	public DisruptionForecastService(String openWeatherApiKey, String eventStorePath) {
		this.openWeatherApiKey = openWeatherApiKey;
		this.eventStorePath = eventStorePath;
		this.gson = new Gson();
	}

	@Override
	public DisruptionForecast forecastDisruption(String airportCode, String date) {
		try {
			if (!isValidAirportCode(airportCode) || !isValidDate(date)) {
				throw new IllegalArgumentException("Invalid airport code or date format");
			}

			JsonObject weatherForecast = getWeatherForecast(airportCode, date);
			double windSpeed = getSafeDouble(weatherForecast, "wind_speed", 0.0);
			double precipitation = getSafeDouble(weatherForecast, "precipitation", 0.0);

			double historicalDelayRate = analyzeHistoricalData(airportCode, windSpeed, precipitation, date);

			double probability = calculateProbability(windSpeed, precipitation, historicalDelayRate);
			logger.info("Calculated probability: {} for wind={}, precipitation={}, historicalDelayRate={}", probability, windSpeed, precipitation, historicalDelayRate);

			DisruptionForecast.Factors factors = new DisruptionForecast.Factors(
					windSpeed + " km/h",
					precipitation + " mm",
					String.format("%.0f%% of flights delayed on similar conditions", historicalDelayRate * 100)
			);
			return new DisruptionForecast(airportCode, date, probability, factors);

		} catch (Exception e) {
			logger.error("Error forecasting disruption: {}", e.getMessage(), e);
			throw new RuntimeException("Error forecasting disruption: " + e.getMessage(), e);
		}
	}

	private JsonObject getWeatherForecast(String airportCode, String date) throws IOException {
		String lat = "28.0445"; // Tenerife North (TFN) example
		String lon = "-16.5725";
		String urlString = String.format(
				"https://api.openweathermap.org/data/2.5/forecast?lat=%s&lon=%s&appid=%s&units=metric",
				lat, lon, openWeatherApiKey
		);

		URL url = new URL(urlString);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod("GET");

		if (conn.getResponseCode() != 200) {
			throw new IOException("Failed to fetch weather forecast: HTTP " + conn.getResponseCode());
		}

		BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		JsonObject response = gson.fromJson(reader, JsonObject.class);
		reader.close();
		conn.disconnect();

		JsonObject forecast = response.getAsJsonArray("list").get(0).getAsJsonObject();
		JsonObject weather = new JsonObject();
		JsonObject wind = forecast.getAsJsonObject("wind");
		weather.addProperty("wind_speed", wind != null ? getSafeDouble(wind, "speed", 0.0) : 0.0);
		JsonObject rain = forecast.getAsJsonObject("rain");
		weather.addProperty("precipitation", rain != null ? getSafeDouble(rain, "3h", 0.0) : 0.0);
		return weather;
	}

	private double analyzeHistoricalData(String airportCode, double windSpeed, double precipitation, String date) {
		List<JsonObject> weatherEvents = EventStoreReader.readEvents(
				eventStorePath + "/prediction.Weather/feederA",
				LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE).minusDays(30),
				LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE)
		);
		List<JsonObject> flightEvents = EventStoreReader.readEvents(
				eventStorePath + "/prediction.Flight/feederB",
				LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE).minusDays(30),
				LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE)
		);

		logger.info("Loaded {} weather events and {} flight events", weatherEvents.size(), flightEvents.size());
		int similarConditions = 0;
		int delayedFlights = 0;

		for (JsonObject weather : weatherEvents) {
			JsonObject weatherData = weather.getAsJsonObject("data");
			if (weatherData == null) {
				logger.warn("Weather event missing 'data' field: {}", weather);
				continue;
			}
			double wSpeed = getSafeDouble(weatherData, "velocidadViento", 0.0);
			double precip = getSafeDouble(weatherData, "precipitation", 0.0);
			String condition = getSafeString(weatherData, "condicion", "");
			// Infer precipitation from condition if missing
			if (precip == 0.0 && condition.toLowerCase().contains("rain")) {
				precip = 0.5; // Minimal precipitation for rainy conditions
			}
			if (Math.abs(wSpeed - windSpeed) < 5 && Math.abs(precip - precipitation) < 2) {
				similarConditions++;
				String weatherTs = getSafeString(weather, "ts", "");
				for (JsonObject flight : flightEvents) {
					JsonObject flightData = flight.getAsJsonObject("data");
					if (flightData == null) {
						logger.warn("Flight event missing 'data' field: {}", flight);
						continue;
					}
					if (getSafeString(flight, "ts", "").equals(weatherTs) && getSafeString(flightData, "estado", "").equals("delayed")) {
						delayedFlights++;
						break;
					}
				}
			}
		}

		logger.info("Found {} similar conditions, {} delayed flights", similarConditions, delayedFlights);
		return similarConditions > 0 ? (double) delayedFlights / similarConditions : 0.0;
	}



	private double calculateProbability(double windSpeed, double precipitation, double historicalDelayRate) {
		double windScore = windSpeed > 10 ? 0.8 : windSpeed > 3 ? 0.4 : 0.0;
		double precipScore = precipitation > 1 ? 0.6 : precipitation > 0.2 ? 0.3 : 0.0;
		return 0.4 * windScore + 0.3 * precipScore + 0.3 * historicalDelayRate;
	}
	private double getSafeDouble(JsonObject json, String key, double defaultValue) {
		JsonElement element = json.get(key);
		return element != null && !element.isJsonNull() ? element.getAsDouble() : defaultValue;
	}

	private String getSafeString(JsonObject json, String key, String defaultValue) {
		JsonElement element = json.get(key);
		return element != null && !element.isJsonNull() ? element.getAsString() : defaultValue;
	}

	private boolean isValidAirportCode(String airportCode) {
		return airportCode != null && airportCode.matches("[A-Z]{3}");
	}

	private boolean isValidDate(String date) {
		try {
			LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE);
			return true;
		} catch (Exception e) {
			return false;
		}
	}
}