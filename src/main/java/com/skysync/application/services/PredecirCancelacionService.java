package com.skysync.application.services;

import com.skysync.application.ports.in.PredecirCancelacionUseCase;
import com.skysync.application.ports.out.ClimaPorCodigoPort;
import com.skysync.domain.model.Clima;

public class PredecirCancelacionService implements PredecirCancelacionUseCase {

	private final ClimaPorCodigoPort climaPort;

	public PredecirCancelacionService(ClimaPorCodigoPort climaPort) {
		this.climaPort = climaPort;
	}

	@Override
	public String predecir(String codigo) {
		Clima clima = climaPort.obtenerClimaPorCodigo(codigo);
		if (clima == null) return "⚠️ No se pudo obtener el clima para " + codigo;

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

	private ResultadoPrediccion calcularProbabilidad(Clima clima) {
		StringBuilder motivos = new StringBuilder();
		double prob = 0;

		if (clima.getVelocidadViento() > 30) {
			motivos.append("Viento muy fuerte. ");
			prob += 35;
		} else if (clima.getVelocidadViento() > 20) {
			motivos.append("Viento elevado. ");
			prob += 20;
		}

		if (clima.getHumedad() > 90) {
			motivos.append("Humedad muy alta. ");
			prob += 20;
		} else if (clima.getHumedad() > 75) {
			motivos.append("Humedad elevada. ");
			prob += 10;
		}

		if (clima.getTemperatura() < 5) {
			motivos.append("Temperatura baja. ");
			prob += 15;
		}

		String c = clima.getCondicion().toLowerCase();
		if (c.contains("rain") || c.contains("storm")) {
			motivos.append("Lluvia o tormenta. ");
			prob += 25;
		} else if (c.contains("fog")) {
			motivos.append("Niebla. ");
			prob += 15;
		}

		prob = Math.min(prob, 100);
		String nivel = prob >= 75 ? "CRÍTICO" : prob >= 50 ? "ALTO" : prob >= 25 ? "MODERADO" : "BAJO";

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
