package com.Skysync.core;

import com.Skysync.api.OpenWeatherAPI;
import com.Skysync.models.Clima;

public class PredictiveEngine {

	private final OpenWeatherAPI climaAPI = new OpenWeatherAPI();

	public void predecir(String ciudad) {
		Clima clima = climaAPI.obtenerClima(ciudad);
		if (clima == null) {
			System.out.println("⚠️ No se pudo obtener el clima de " + ciudad);
			return;
		}

		System.out.println("\n📍 Clima actual en " + ciudad + ": " + clima);
		double prob = calcularProbabilidad(clima);
		System.out.printf("🔮 Probabilidad estimada de cancelación/retraso: %.1f%%\n", prob);
	}

	private double calcularProbabilidad(Clima clima) {
		double prob = 0;
		if (clima.getVelocidadViento() > 25) prob += 40;
		if (clima.getHumedad() > 85) prob += 30;
		if (clima.getTemperatura() < 5) prob += 10;

		return Math.min(prob, 95);
	}
}
