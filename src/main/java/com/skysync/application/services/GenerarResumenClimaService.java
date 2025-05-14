package com.skysync.application.services;

import com.skysync.application.ports.out.ClimaRepository;
import com.skysync.domain.model.Clima;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class GenerarResumenClimaService {

	private final ClimaRepository repo;

	public GenerarResumenClimaService(ClimaRepository repo) {
		this.repo = repo;
	}

	public String generarResumen() {
		Map<String, List<Clima>> porCiudad = repo.obtenerTodos()
				.stream()
				.collect(Collectors.groupingBy(Clima::getCiudad));

		StringBuilder sb = new StringBuilder("📊 Resumen de Clima Promedio:\n");

		porCiudad.forEach((ciudad, climas) -> {
			double t = climas.stream().mapToDouble(Clima::getTemperatura).average().orElse(0.0);
			double h = climas.stream().mapToDouble(Clima::getHumedad).average().orElse(0.0);
			double v = climas.stream().mapToDouble(Clima::getVelocidadViento).average().orElse(0.0);

			sb.append(String.format("- %s → 🌡️ %.1f°C, 💧 %.0f%%, 💨 %.1f km/h\n", ciudad, t, h, v));
		});

		return sb.toString();
	}
}
