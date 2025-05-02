package com.Skysync.core;

import com.Skysync.feeders.weather.OpenWeatherAPI;
import com.Skysync.models.Clima;

import java.util.HashMap;
import java.util.Map;

public class PredictiveEngine {

	private final OpenWeatherAPI climaAPI = new OpenWeatherAPI();

	private static final Map<String, String> IATA_TO_CIUDAD = new HashMap<>();

	static {
		IATA_TO_CIUDAD.put("LPA", "Las Palmas");
		IATA_TO_CIUDAD.put("TFN", "San Cristóbal de La Laguna");
		IATA_TO_CIUDAD.put("TFS", "Granadilla de Abona");
		IATA_TO_CIUDAD.put("ACE", "Arrecife");
		IATA_TO_CIUDAD.put("FUE", "Puerto del Rosario");
		IATA_TO_CIUDAD.put("SPC", "Santa Cruz de La Palma");
		IATA_TO_CIUDAD.put("GMZ", "San Sebastián de La Gomera");
		IATA_TO_CIUDAD.put("VDE", "Valverde");
	}

	public String predecirComoTexto(String codigo) {
		Clima clima = climaAPI.obtenerClimaPorCodigo(codigo);

		if (clima == null) {
			return "⚠️ No se pudo obtener el clima para " + codigo;
		}

		ResultadoPrediccion resultado = calcularProbabilidad(clima);

		return String.format("""
		📍 Aeropuerto %s (%s)
		🌤️ Clima actual: %s
		🔮 Riesgo estimado: %s (%.1f%%)
		🧠 Motivo: %s
		""",
				codigo, clima.getCiudad(), clima.toString(),
				resultado.nivel, resultado.probabilidad,
				resultado.motivo);
	}

	private double evaluarViento(double velocidad, StringBuilder motivos) {
		if (velocidad > 30) {
			motivos.append("Viento muy fuerte. ");
			return 35;
		} else if (velocidad > 20) {
			motivos.append("Viento elevado. ");
			return 20;
		}
		return 0;
	}

	private double evaluarHumedad(double humedad, StringBuilder motivos) {
		if (humedad > 90) {
			motivos.append("Humedad muy alta. ");
			return 20;
		} else if (humedad > 75) {
			motivos.append("Humedad elevada. ");
			return 10;
		}
		return 0;
	}

	private double evaluarTemperatura(double temperatura, StringBuilder motivos) {
		if (temperatura < 5) {
			motivos.append("Temperatura baja. ");
			return 15;
		}
		return 0;
	}

	private double evaluarCondicion(String condicion, StringBuilder motivos) {
		String c = condicion.toLowerCase();
		if (c.contains("rain") || c.contains("storm")) {
			motivos.append("Lluvia o tormenta detectada. ");
			return 25;
		} else if (c.contains("fog")) {
			motivos.append("Niebla o baja visibilidad. ");
			return 15;
		}
		return 0;
	}


	private static class ResultadoPrediccion {
		double probabilidad;
		String nivel;
		String motivo;

		public ResultadoPrediccion(double probabilidad, String nivel, String motivo) {
			this.probabilidad = probabilidad;
			this.nivel = nivel;
			this.motivo = motivo;
		}
	}

	public void predecir(String codigo) {
		System.out.println(predecirComoTexto(codigo));
	}

	private ResultadoPrediccion calcularProbabilidad(Clima clima) {
		StringBuilder motivos = new StringBuilder();
		double prob = 0;

		prob += evaluarViento(clima.getVelocidadViento(), motivos);
		prob += evaluarHumedad(clima.getHumedad(), motivos);
		prob += evaluarTemperatura(clima.getTemperatura(), motivos);
		prob += evaluarCondicion(clima.getCondicion(), motivos);

		prob = Math.min(prob, 100);

		String nivel;
		if (prob >= 75) nivel = "CRÍTICO";
		else if (prob >= 50) nivel = "ALTO";
		else if (prob >= 25) nivel = "MODERADO";
		else nivel = "BAJO";

		return new ResultadoPrediccion(prob, nivel, motivos.toString().trim());
	}

}
