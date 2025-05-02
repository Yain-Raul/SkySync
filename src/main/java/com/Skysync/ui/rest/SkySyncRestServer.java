package com.Skysync.ui.rest;

import com.Skysync.business.BusinessUnit;
import com.Skysync.feeders.weather.ClimaCollector;
import com.Skysync.feeders.flights.DataCollector;
import com.Skysync.core.InformeGenerator;
import com.Skysync.core.PredictiveEngine;
import io.javalin.Javalin;

public class SkySyncRestServer {

	public static void main(String[] args) {

		BusinessUnit business = new BusinessUnit();
		InformeGenerator informeGenerator = new InformeGenerator();
		PredictiveEngine predictiveEngine = new PredictiveEngine();

		Javalin app = Javalin.create().start(7000);

		app.before(ctx -> {
			ctx.header("Access-Control-Allow-Origin", "*");
			ctx.header("Content-Type", "application/json");
		});

		new Thread(() -> {
			System.out.println("📈 Iniciando BusinessUnit en segundo plano...");
			new BusinessUnit().iniciar();
		}).start();

		System.out.println("🚀 API REST de SkySync disponible en http://localhost:7000");

		// 📥 Recolección
		app.post("/recolectar/clima", ctx -> {
			new ClimaCollector().recolectarClimaActual();
			ctx.result("{\"message\":\"✅ Recolección de clima lanzada\"}");
		});

		app.post("/recolectar/vuelos", ctx -> {
			new DataCollector().recolectarVuelosPorAeropuerto();
			ctx.result("{\"message\":\"✅ Recolección de vuelos lanzada\"}");
		});

		// 📝 Informe del día
		app.get("/informe", ctx -> {
			String fecha = ctx.queryParam("fecha");
			if (fecha == null || fecha.isBlank()) {
				ctx.status(400).result("{\"error\":\"⚠️ Fecha no especificada\"}");
				return;
			}
			String resumen = informeGenerator.generarResumenComoTexto(fecha);
			ctx.result("{\"informe\":\"" + resumen.replace("\n", "\\n") + "\"}");
		});

		// 🔮 Predicción por aeropuerto
		app.get("/prediccion", ctx -> {
			String codigo = ctx.queryParam("codigo");
			if (codigo == null || codigo.isBlank()) {
				ctx.status(400).result("{\"error\":\"⚠️ Código IATA no especificado\"}");
				return;
			}
			String prediccion = predictiveEngine.predecirComoTexto(codigo);
			ctx.result("{\"prediccion\":\"" + prediccion.replace("\n", "\\n") + "\"}");
		});

		// 📊 Clima promedio
		app.get("/clima/promedio", ctx -> {
			String resultado = business.resumenComoTexto();
			ctx.result("{\"resumen\":\"" + resultado.replace("\n", "\\n") + "\"}");
		});

		// 🛬 Estado de vuelos
		app.get("/vuelos/estado", ctx -> {
			String resultado = business.estadoVuelosComoTexto();
			ctx.result("{\"estado\":\"" + resultado.replace("\n", "\\n") + "\"}");
		});

		// 🌩️ Condiciones meteorológicas extremas
		app.get("/clima/extremos", ctx -> {
			String resultado = business.condicionesExtremasComoTexto();
			ctx.result("{\"extremos\":\"" + resultado.replace("\n", "\\n") + "\"}");
		});

		// 🚨 Alerta combinada clima + vuelos
		app.get("/alerta/combinada", ctx -> {
			String resultado = business.alertaCombinadaComoTexto();
			ctx.result("{\"alerta\":\"" + resultado.replace("\n", "\\n") + "\"}");
		});
	}
}
