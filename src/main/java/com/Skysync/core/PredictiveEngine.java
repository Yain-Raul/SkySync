package com.Skysync.core;

import com.Skysync.api.OpenWeatherAPI;
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

	public void predecir(String iata) {
		iata = iata.toUpperCase();

		if (!IATA_TO_CIUDAD.containsKey(iata)) {
			System.out.println("❌ Código IATA no válido. Opciones: " + IATA_TO_CIUDAD.keySet());
			return;
		}

		String ciudad = IATA_TO_CIUDAD.get(iata);
		Clima clima = climaAPI.obtenerClima(ciudad);

		if (clima == null) {
			System.out.println("⚠️ No se pudo obtener el clima de " + ciudad);
			return;
		}

		System.out.println("\n📍 Aeropuerto " + iata + " (" + ciudad + ")");
		System.out.println("🌤️ Clima actual: " + clima);

		ResultadoPrediccion resultado = calcularProbabilidad(clima);
		System.out.printf("🔮 Riesgo estimado: %s (%.1f%%)\n", resultado.nivel, resultado.probabilidad);
		if (!resultado.motivo.isEmpty()) {
			System.out.println("🧠 Motivo: " + resultado.motivo);
		}
	}

	private ResultadoPrediccion calcularProbabilidad(Clima clima) {
		double prob = 0;
		StringBuilder motivos = new StringBuilder();

		if (clima.getVelocidadViento() > 30) {
			prob += 35;
			motivos.append("Viento muy fuerte. ");
		} else if (clima.getVelocidadViento() > 20) {
			prob += 20;
			motivos.append("Viento elevado. ");
		}

		if (clima.getHumedad() > 90) {
			prob += 20;
			motivos.append("Humedad muy alta. ");
		} else if (clima.getHumedad() > 75) {
			prob += 10;
			motivos.append("Humedad elevada. ");
		}

		if (clima.getTemperatura() < 5) {
			prob += 15;
			motivos.append("Temperatura baja. ");
		}

		if (clima.getCondicion().toLowerCase().contains("rain") ||
				clima.getCondicion().toLowerCase().contains("storm")) {
			prob += 25;
			motivos.append("Lluvia o tormenta detectada. ");
		} else if (clima.getCondicion().toLowerCase().contains("fog")) {
			prob += 15;
			motivos.append("Niebla o baja visibilidad. ");
		}

		prob = Math.min(prob, 100);

		String nivel;
		if (prob >= 75) nivel = "CRÍTICO";
		else if (prob >= 50) nivel = "ALTO";
		else if (prob >= 25) nivel = "MODERADO";
		else nivel = "BAJO";

		return new ResultadoPrediccion(prob, nivel, motivos.toString().trim());
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
}
