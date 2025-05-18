package com.skysync.adapters.in.rest;

import com.google.gson.Gson;
import com.skysync.application.services.*;
import com.skysync.feederweather.adapters.out.api.OpenWeatherClimaAdapter;
import com.skysync.adapters.out.persistence.SQLiteClimaRepository;
import com.skysync.adapters.out.persistence.SQLiteVueloRepository;
import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.ArrayList; // Added import
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

public class SkySyncRestServer {

	private static final Logger logger = LoggerFactory.getLogger(SkySyncRestServer.class);
	private static final Cache<String, String> cache = Caffeine.newBuilder()
			.expireAfterWrite(10, TimeUnit.MINUTES)
			.maximumSize(100)
			.build();
	private static final Gson gson = new Gson();

	// Date format validation (YYYY-MM-DD)
	private static final Pattern DATE_PATTERN = Pattern.compile("\\d{4}-\\d{2}-\\d{2}");
	// IATA code validation (3 uppercase letters)
	private static final Pattern IATA_PATTERN = Pattern.compile("[A-Z]{3}");

	// Utility method to create a standardized response
	private static String createResponse(String status, Object data, String message) {
		Map<String, Object> response = new HashMap<>();
		response.put("status", status);
		response.put("data", data);
		if (message != null && !message.isEmpty()) {
			response.put("message", message);
		}
		return gson.toJson(response);
	}

	// Validate date format
	private static boolean isValidDate(String fecha) {
		return fecha != null && DATE_PATTERN.matcher(fecha).matches();
	}

	// Validate IATA code
	private static boolean isValidIATA(String iata) {
		return iata != null && IATA_PATTERN.matcher(iata).matches();
	}

	public static void main(String[] args) {
		logger.info("Starting SkySync REST Server...");

		String openWeatherApiKey = System.getenv("OPENWEATHER_API_KEY");
		if (openWeatherApiKey == null) {
			logger.error("OPENWEATHER_API_KEY environment variable not set");
			System.err.println("⚠️ OPENWEATHER_API_KEY environment variable not set");
			return;
		}

		var forecastService = new DisruptionForecastService(openWeatherApiKey, "eventstore");

		Javalin app = Javalin.create(config -> {
			config.staticFiles.add(staticFiles -> {
				staticFiles.directory = "/public";
				staticFiles.hostedPath = "/ui";
				staticFiles.location = Location.CLASSPATH;
			});
		}).start(7000);

		try {
			java.awt.Desktop.getDesktop().browse(new java.net.URI("http://localhost:7000/ui/SkySyncWeb.html"));
		} catch (Exception e) {
			System.err.println("❌ No se pudo abrir el navegador automáticamente.");
			e.printStackTrace();
		}
		app.before(ctx -> {
			ctx.header("Access-Control-Allow-Origin", "*");
			ctx.header("Content-Type", "application/json");
		});

		app.options("/recolectar/*", ctx -> {
			ctx.header("Access-Control-Allow-Methods", "POST, OPTIONS");
			ctx.header("Access-Control-Allow-Headers", "Content-Type");
			ctx.status(200);
		});

		System.out.println("🚀 API REST de SkySync disponible en http://localhost:7000");

		// /vuelos/estado endpoint
		app.get("/vuelos/estado", ctx -> {
			logger.info("Request to /vuelos/estado with from={}, to={}, iata={}, limit={}, offset={}",
					ctx.queryParam("from"), ctx.queryParam("to"), ctx.queryParam("iata"),
					ctx.queryParam("limit"), ctx.queryParam("offset"));

			String fromDate = ctx.queryParam("from");
			String toDate = ctx.queryParam("to");
			String iata = ctx.queryParam("iata");
			int limit;
			int offset;
			try {
				limit = ctx.queryParam("limit") != null ? Integer.parseInt(ctx.queryParam("limit")) : 10;
				offset = ctx.queryParam("offset") != null ? Integer.parseInt(ctx.queryParam("offset")) : 0;
			} catch (NumberFormatException e) {
				ctx.status(400).result(createResponse("error", null, "⚠️ Limit o offset inválido. Use valores numéricos"));
				return;
			}

			if (fromDate != null && !fromDate.isBlank() && !isValidDate(fromDate)) {
				ctx.status(400).result(createResponse("error", null, "⚠️ Formato de fecha 'from' inválido. Use YYYY-MM-DD, por ejemplo: 2025-05-18"));
				return;
			}
			if (toDate != null && !toDate.isBlank() && !isValidDate(toDate)) {
				ctx.status(400).result(createResponse("error", null, "⚠️ Formato de fecha 'to' inválido. Use YYYY-MM-DD, por ejemplo: 2025-05-18"));
				return;
			}
			if (iata != null && !iata.isBlank() && !isValidIATA(iata)) {
				ctx.status(400).result(createResponse("error", null, "⚠️ Código IATA inválido. Use 3 letras mayúsculas, por ejemplo: TFN"));
				return;
			}

			String effectiveFrom = fromDate != null ? fromDate : LocalDate.now().toString();
			String effectiveTo = toDate != null ? toDate : LocalDate.now().toString();
			logger.info("Effective date range: from={}, to={}", effectiveFrom, effectiveTo);

			String cacheKey = String.format("vuelos_estado_%s_%s_%s_%d_%d",
					effectiveFrom, effectiveTo, iata != null ? iata.toUpperCase() : "all", limit, offset);
			String cachedResult = cache.getIfPresent(cacheKey);
			if (cachedResult != null) {
				logger.debug("Returning cached result for /vuelos/estado with key {}", cacheKey);
				ctx.result(cachedResult);
				return;
			}

			try {
				var vueloRepo = new SQLiteVueloRepository();
				var servicio = new ConsultarEstadoVuelosService(vueloRepo);

				Map<String, Object> result = servicio.obtenerEstado(fromDate, toDate, iata, limit, offset);

				Map<String, Object> data = (Map<String, Object>) result.get("data");
				List<?> summary = (List<?>) data.get("summary");
				List<?> flights = (List<?>) data.get("flights");
				if (summary.isEmpty() && flights.isEmpty()) {
					result = new HashMap<>();
					result.put("status", "success");
					result.put("data", Map.of(
							"summary", new ArrayList<>(),
							"flights", new ArrayList<>(),
							"dateRange", Map.of("from", effectiveFrom, "to", effectiveTo),
							"pagination", Map.of("limit", limit, "offset", offset, "total", 0),
							"links", Map.of(
									"self", String.format("/vuelos/estado?from=%s&to=%s&iata=%s&limit=%d&offset=%d",
											effectiveFrom, effectiveTo, iata != null ? iata : "", limit, offset),
									"next", String.format("/vuelos/estado?from=%s&to=%s&iata=%s&limit=%d&offset=%d",
											effectiveFrom, effectiveTo, iata != null ? iata : "", limit, offset + limit)
							)
					));
					result.put("message", "No hay datos de vuelos disponibles para los parámetros especificados");
				}

				String response = gson.toJson(result);
				cache.put(cacheKey, response);
				ctx.result(response);
			} catch (Exception e) {
				logger.error("Error retrieving flight status: {}", e.getMessage(), e);
				ctx.status(500).result(createResponse("error", null, "⚠️ Error interno del servidor: " + e.getMessage()));
			}
		});

		// Other endpoints (unchanged)
		app.post("/recolectar/clima", ctx -> {
			logger.info("Request to /recolectar/clima");
			ctx.result(createResponse("info", null, "⚠️ Recolección automática se realiza desde CLI programado"));
		});

		app.post("/recolectar/vuelos", ctx -> {
			logger.info("Request to /recolectar/vuelos");
			ctx.result(createResponse("info", null, "⚠️ Recolección automática se realiza desde CLI programado"));
		});

		app.get("/informe", ctx -> {
			logger.info("Request to /informe with fecha={}, iata={}, sort={}", ctx.queryParam("fecha"), ctx.queryParam("iata"), ctx.queryParam("sort"));
			String fecha = ctx.queryParam("fecha");
			String iata = ctx.queryParam("iata");
			String sort = ctx.queryParam("sort");

			if (fecha == null || fecha.isBlank()) {
				ctx.status(400).result(createResponse("error", null, "⚠️ Fecha no especificada. Use formato YYYY-MM-DD, por ejemplo: 2025-05-18"));
				return;
			}
			if (!isValidDate(fecha)) {
				ctx.status(400).result(createResponse("error", null, "⚠️ Formato de fecha inválido. Use YYYY-MM-DD, por ejemplo: 2025-05-18"));
				return;
			}
			if (iata != null && !iata.isBlank() && !isValidIATA(iata)) {
				ctx.status(400).result(createResponse("error", null, "⚠️ Código IATA inválido. Use 3 letras mayúsculas, por ejemplo: TFN"));
				return;
			}
			if (sort != null && !sort.isBlank() && !sort.equalsIgnoreCase("delay_asc") && !sort.equalsIgnoreCase("delay_desc")) {
				ctx.status(400).result(createResponse("error", null, "⚠️ Parámetro 'sort' inválido. Use 'delay_asc' o 'delay_desc'"));
				return;
			}

			var climaRepo = new SQLiteClimaRepository();
			var vueloRepo = new SQLiteVueloRepository();
			var servicio = new GenerarInformeService(climaRepo, vueloRepo);

			try {
				Map<String, Object> data = servicio.generarInforme(fecha, iata, sort);
				ctx.result(createResponse("success", data, null));
			} catch (Exception e) {
				logger.error("Error generating informe: {}", e.getMessage(), e);
				ctx.status(500).result(createResponse("error", null, "⚠️ Error interno del servidor: " + e.getMessage()));
			}
		});

		app.get("/prediccion", ctx -> {
			logger.info("Request to /prediccion with codigo={}, fecha={}", ctx.queryParam("codigo"), ctx.queryParam("fecha"));
			String codigo = ctx.queryParam("codigo");
			String fecha = ctx.queryParam("fecha");

			if (codigo == null || codigo.isBlank()) {
				ctx.status(400).result(createResponse("error", null, "⚠️ Código IATA no especificado. Use un código válido, por ejemplo: TFN"));
				return;
			}
			if (!isValidIATA(codigo)) {
				ctx.status(400).result(createResponse("error", null, "⚠️ Código IATA inválido. Use 3 letras mayúsculas, por ejemplo: TFN"));
				return;
			}
			if (fecha != null && !fecha.isBlank() && !isValidDate(fecha)) {
				ctx.status(400).result(createResponse("error", null, "⚠️ Formato de fecha inválido. Use YYYY-MM-DD, por ejemplo: 2025-05-18"));
				return;
			}

			var climaPort = new OpenWeatherClimaAdapter();
			var servicio = new PredecirCancelacionService(climaPort);

			try {
				String resultado;
				if (fecha != null && !fecha.isBlank()) {
					logger.warn("Fecha parameter provided but PredecirCancelacionService.predecir(codigo, fecha) is not implemented. Ignoring fecha.");
					resultado = servicio.predecir(codigo);
				} else {
					resultado = servicio.predecir(codigo);
				}

				Map<String, Object> data = new HashMap<>();
				data.put("prediccion", resultado.replace("\n", "\\n"));
				data.put("codigo", codigo);
				if (fecha != null) data.put("fecha", fecha);

				ctx.result(createResponse("success", data, null));
			} catch (Exception e) {
				logger.error("Error predicting cancellations: {}", e.getMessage(), e);
				ctx.status(500).result(createResponse("error", null, "⚠️ Error interno del servidor: " + e.getMessage()));
			}
		});

		app.get("/clima/promedio", ctx -> {
			int limit = ctx.queryParam("limit") != null ? Integer.parseInt(ctx.queryParam("limit")) : 10;
			int offset = ctx.queryParam("offset") != null ? Integer.parseInt(ctx.queryParam("offset")) : 0;
			var service = new GenerarResumenClimaService(new SQLiteClimaRepository());
			Map<String, Object> result = service.generarResumen(limit, offset);
			ctx.json(result);
		});

		app.get("/clima/extremos", ctx -> {
			logger.info("Request to /clima/extremos");
			String cacheKey = "clima_extremos";
			String cachedResult = cache.getIfPresent(cacheKey);
			if (cachedResult != null) {
				logger.debug("Returning cached result for /clima/extremos");
				ctx.result(cachedResult);
				return;
			}

			try {
				var climaRepo = new SQLiteClimaRepository();
				var servicio = new DetectarCondicionesExtremasService(climaRepo);
				String resultado = servicio.detectar();

				Map<String, Object> data = new HashMap<>();
				data.put("extremos", resultado.replace("\n", "\\n"));
				String response = createResponse("success", data, null);

				cache.put(cacheKey, response);
				ctx.result(response);
			} catch (Exception e) {
				logger.error("Error detecting extreme weather: {}", e.getMessage(), e);
				ctx.status(500).result(createResponse("error", null, "⚠️ Error interno del servidor: " + e.getMessage()));
			}
		});

		app.get("/alerta/combinada", ctx -> {
			logger.info("Request to /alerta/combinada");
			String cacheKey = "alerta_combinada";
			String cachedResult = cache.getIfPresent(cacheKey);
			if (cachedResult != null) {
				logger.debug("Returning cached result for /alerta/combinada");
				ctx.result(cachedResult);
				return;
			}

			try {
				var climaRepo = new SQLiteClimaRepository();
				var vueloRepo = new SQLiteVueloRepository();
				var servicio = new DetectarAlertaCombinadaService(climaRepo, vueloRepo);
				String resultado = servicio.evaluar();

				Map<String, Object> data = new HashMap<>();
				data.put("alerta", resultado.replace("\n", "\\n"));
				String response = createResponse("success", data, null);

				cache.put(cacheKey, response);
				ctx.result(response);
			} catch (Exception e) {
				logger.error("Error evaluating combined alert: {}", e.getMessage(), e);
				ctx.status(500).result(createResponse("error", null, "⚠️ Error interno del servidor: " + e.getMessage()));
			}
		});

		app.get("/forecast/disruption", ctx -> {
			logger.info("Request to /forecast/disruption with codigo={}, fecha={}", ctx.queryParam("codigo"), ctx.queryParam("fecha"));
			String codigo = ctx.queryParam("codigo");
			String fecha = ctx.queryParam("fecha");
			String limit = ctx.queryParam("limit");
			String offset = ctx.queryParam("offset");

			if (limit == null || limit.isBlank()) {
				limit = "10";
			}
			if (offset == null || offset.isBlank()) {
				offset = "0";
			}

			if (codigo == null || codigo.isBlank()) {
				ctx.status(400).result(createResponse("error", null, "⚠️ Código IATA no especificado. Use un código válido, por ejemplo: TFN"));
				return;
			}
			if (!isValidIATA(codigo)) {
				ctx.status(400).result(createResponse("error", null, "⚠️ Código IATA inválido. Use 3 letras mayúsculas, por ejemplo: TFN"));
				return;
			}
			if (fecha == null || fecha.isBlank()) {
				ctx.status(400).result(createResponse("error", null, "⚠️ Fecha no especificada. Use formato YYYY-MM-DD, por ejemplo: 2025-05-18"));
				return;
			}
			if (!isValidDate(fecha)) {
				ctx.status(400).result(createResponse("error", null, "⚠️ Formato de fecha inválido. Use YYYY-MM-DD, por ejemplo: 2025-05-18"));
				return;
			}

			try {
				int limitInt = Integer.parseInt(limit);
				int offsetInt = Integer.parseInt(offset);
				var forecast = forecastService.forecastDisruption(codigo, fecha);

				Map<String, Object> data = new HashMap<>();
				data.put("airportCode", forecast.getAirportCode());
				data.put("date", forecast.getDate());
				data.put("disruptionProbability", forecast.getDisruptionProbability());
				data.put("factors", forecast.getFactors());
				data.put("pagination", Map.of("limit", limitInt, "offset", offsetInt, "total", 1));
				data.put("links", Map.of(
						"self", "/forecast/disruption?codigo=" + codigo + "&fecha=" + fecha,
						"next", "/forecast/disruption?codigo=" + codigo + "&fecha=" + fecha + "&limit=" + limit + "&offset=" + (offsetInt + limitInt)
				));

				ctx.result(createResponse("success", data, null));
			} catch (NumberFormatException e) {
				logger.error("Invalid limit or offset: {}", e.getMessage());
				ctx.status(400).result(createResponse("error", null, "⚠️ Limit o offset inválido. Use valores numéricos, por ejemplo: limit=10, offset=0"));
			} catch (IllegalArgumentException e) {
				logger.error("Illegal argument: {}", e.getMessage());
				ctx.status(400).result(createResponse("error", null, "⚠️ " + e.getMessage()));
			} catch (Exception e) {
				logger.error("Server error: {}", e.getMessage(), e);
				ctx.status(500).result(createResponse("error", null, "⚠️ Error interno del servidor: " + e.getMessage()));
			}
		});

		app.get("/", ctx -> {
			logger.info("Request to root endpoint");
			Map<String, Object> rootResponse = new HashMap<>();
			rootResponse.put("message", "🚀 SkySync API está en ejecución.");
			rootResponse.put("endpoints", Map.of(
					"/vuelos/estado", Map.of(
							"method", "GET",
							"description", "Muestra el estado de los vuelos con resumen por aeropuerto y lista detallada.",
							"parameters", Map.of(
									"from", "Fecha inicial (YYYY-MM-DD, opcional)",
									"to", "Fecha final (YYYY-MM-DD, opcional)",
									"iata", "Código IATA del aeropuerto (opcional, ej: TFN)",
									"limit", "Límite de resultados por página (opcional, default: 10)",
									"offset", "Offset para paginación (opcional, default: 0)"
							),
							"example", "/vuelos/estado?from=2025-05-18&to=2025-05-18&iata=TFN&limit=10&offset=0"
					),
					"/informe", Map.of(
							"method", "GET",
							"description", "Genera un informe de clima y vuelos para una fecha específica.",
							"parameters", Map.of(
									"fecha", "Fecha en formato YYYY-MM-DD (requerido)",
									"iata", "Código IATA del aeropuerto (opcional, ej: TFN)",
									"sort", "Ordenar resultados: delay_asc, delay_desc (opcional)"
							),
							"example", "/informe?fecha=2025-05-18&iata=TFN"
					),
					"/prediccion", Map.of(
							"method", "GET",
							"description", "Predice cancelaciones de vuelos basadas en un código IATA.",
							"parameters", Map.of(
									"codigo", "Código IATA del aeropuerto (requerido, ej: TFN)",
									"fecha", "Fecha en formato YYYY-MM-DD (opcional, predice para hoy si no se especifica)"
							),
							"example", "/prediccion?codigo=TFN&fecha=2025-05-18"
					),
					"/clima/promedio", Map.of(
							"method", "GET",
							"description", "Muestra un resumen del clima promedio.",
							"parameters", "Ninguno",
							"example", "/clima/promedio"
					),
					"/clima/extremos", Map.of(
							"method", "GET",
							"description", "Detecta condiciones climáticas extremas.",
							"parameters", "Ninguno",
							"example", "/clima/extremos"
					),
					"/alerta/combinada", Map.of(
							"method", "GET",
							"description", "Evalúa alertas combinadas de clima y vuelos.",
							"parameters", "Ninguno",
							"example", "/alerta/combinada"
					),
					"/forecast/disruption", Map.of(
							"method", "GET",
							"description", "Pronostica interrupciones de vuelos para un aeropuerto y fecha.",
							"parameters", Map.of(
									"codigo", "Código IATA del aeropuerto (requerido, ej: TFN)",
									"fecha", "Fecha en formato YYYY-MM-DD (requerido)",
									"limit", "Límite de resultados por página (opcional, default: 10)",
									"offset", "Offset para paginación (opcional, default: 0)"
							),
							"example", "/forecast/disruption?codigo=TFN&fecha=2025-05-18&limit=10&offset=0"
					)
			));
			ctx.result(gson.toJson(rootResponse));
		});
	}
}