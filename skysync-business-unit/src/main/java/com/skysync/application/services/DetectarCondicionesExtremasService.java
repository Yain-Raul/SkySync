package com.skysync.application.services;

import com.skysync.core.aplication.ports.out.ClimaRepository;
import com.skysync.core.domain.model.Clima;

import java.util.List;

public class DetectarCondicionesExtremasService {

	private final ClimaRepository repo;

	public DetectarCondicionesExtremasService(ClimaRepository repo) {
		this.repo = repo;
	}

	public String detectar() {
		List<Clima> extremos = repo.obtenerTodos().stream()
				.filter(c -> c.getVelocidadViento() > 30 || c.getHumedad() > 90)
				.toList();

		if (extremos.isEmpty()) return "✅ No se detectaron condiciones extremas.";

		StringBuilder sb = new StringBuilder("🌩️ Condiciones Meteorológicas Extremas:\n");
		for (Clima c : extremos) {
			sb.append(String.format("- %s → 🌡️ %.1f°C, 💧 %.0f%%, 💨 %.1f km/h, ☁️ %s\n",
					c.getCiudad(), c.getTemperatura(), c.getHumedad(), c.getVelocidadViento(), c.getCondicion()));
		}

		return sb.toString();
	}
}
