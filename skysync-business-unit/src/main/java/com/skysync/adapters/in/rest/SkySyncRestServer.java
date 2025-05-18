package com.skysync.adapters.in.rest;

import com.skysync.application.services.*;
import com.skysync.feederweather.adapters.out.api.OpenWeatherClimaAdapter;
import com.skysync.adapters.out.persistence.SQLiteClimaRepository;
import com.skysync.adapters.out.persistence.SQLiteVueloRepository;

import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;



public class SkySyncRestServer {

	public static void main(String[] args) {

		Javalin app = Javalin.create(config -> {
			config.staticFiles.add(staticFiles -> {
				staticFiles.directory = "/public";
				staticFiles.hostedPath = "/ui";
				staticFiles.location = Location.CLASSPATH;
			});
		}).start(7000);

		app.before(ctx -> {
			ctx.header("Access-Control-Allow-Origin", "*");
			ctx.header("Content-Type", "application/json");
		});

		System.out.println("🚀 API REST de SkySync disponible en http://localhost:7000");

		try {
			java.awt.Desktop.getDesktop().browse(new java.net.URI("http://localhost:7000/ui/SkySyncWeb.html"));
		} catch (Exception e) {
			System.err.println("❌ No se pudo abrir automáticamente el navegador.");
			e.printStackTrace();
		}

		// 🔹 Recolectar clima y vuelos
		app.post("/recolectar/clima", ctx -> {
			ctx.result("{\"message\":\"⚠️ Recolección automática se realiza desde CLI programado\"}");
		});

		app.post("/recolectar/vuelos", ctx -> {
			ctx.result("{\"message\":\"⚠️ Recolección automática se realiza desde CLI programado\"}");
		});

		// 🔹 Generar informe por fecha
		app.get("/informe", ctx -> {
			String fecha = ctx.queryParam("fecha");
			if (fecha == null || fecha.isBlank()) {
				ctx.status(400).result("{\"error\":\"⚠️ Fecha no especificada\"}");
				return;
			}

			var climaRepo = new SQLiteClimaRepository();
			var vueloRepo = new SQLiteVueloRepository();
			var servicio = new GenerarInformeService(climaRepo, vueloRepo);

			String resultado = servicio.generarInforme(fecha);
			ctx.result("{\"informe\":\"" + resultado.replace("\n", "\\n") + "\"}");
		});

		// 🔹 Predicción por IATA
		app.get("/prediccion", ctx -> {
			String codigo = ctx.queryParam("codigo");
			if (codigo == null || codigo.isBlank()) {
				ctx.status(400).result("{\"error\":\"⚠️ Código IATA no especificado\"}");
				return;
			}

			var climaPort = new OpenWeatherClimaAdapter();
			var servicio = new PredecirCancelacionService(climaPort);

			String resultado = servicio.predecir(codigo);
			ctx.result("{\"prediccion\":\"" + resultado.replace("\n", "\\n") + "\"}");
		});

		// 🔹 Clima promedio
		app.get("/clima/promedio", ctx -> {
			var climaRepo = new SQLiteClimaRepository();
			var servicio = new GenerarResumenClimaService(climaRepo);
			String resultado = servicio.generarResumen();
			ctx.result("{\"resumen\":\"" + resultado.replace("\n", "\\n") + "\"}");
		});

		// 🔹 Estado de vuelos
		app.get("/vuelos/estado", ctx -> {
			var vueloRepo = new SQLiteVueloRepository();
			var servicio = new ConsultarEstadoVuelosService(vueloRepo);
			String resultado = servicio.obtenerEstado();
			ctx.result("{\"estado\":\"" + resultado.replace("\n", "\\n") + "\"}");
		});

		app.get("/clima/extremos", ctx -> {
			var climaRepo = new SQLiteClimaRepository();
			var servicio = new DetectarCondicionesExtremasService(climaRepo);
			String resultado = servicio.detectar();
			ctx.result("{\"extremos\":\"" + resultado.replace("\n", "\\n") + "\"}");
		});

		// 🔹 Alerta combinada clima + vuelos
		app.get("/alerta/combinada", ctx -> {
			var climaRepo = new SQLiteClimaRepository();
			var vueloRepo = new SQLiteVueloRepository();
			var servicio = new DetectarAlertaCombinadaService(climaRepo, vueloRepo);
			String resultado = servicio.evaluar();
			ctx.result("{\"alerta\":\"" + resultado.replace("\n", "\\n") + "\"}");
		});

		app.get("/", ctx -> {
			ctx.result("""
			{
			  "message": "🚀 SkySync API está en ejecución.",
			  "endpoints": [
				"/informe?fecha=YYYY-MM-DD",
				"/prediccion?codigo=IATA",
				"/clima/promedio",
				"/clima/extremos",
				"/vuelos/estado",
				"/alerta/combinada"
			  ]
			}
			""");
		});

	}

}
