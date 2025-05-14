package com.skysync.application.services;

import com.skysync.application.ports.out.ClimaRepository;
import com.skysync.application.ports.out.VueloRepository;
import com.skysync.domain.model.Clima;
import com.skysync.domain.model.Vuelo;

import java.util.List;

public class DetectarAlertaCombinadaService {

	private final ClimaRepository climaRepo;
	private final VueloRepository vueloRepo;

	public DetectarAlertaCombinadaService(ClimaRepository climaRepo, VueloRepository vueloRepo) {
		this.climaRepo = climaRepo;
		this.vueloRepo = vueloRepo;
	}

	public String evaluar() {
		long extremos = climaRepo.obtenerTodos().stream()
				.filter(c -> c.getVelocidadViento() > 30 || c.getHumedad() > 90)
				.count();

		long retrasados = vueloRepo.obtenerTodos().stream()
				.filter(v -> v.getEstado().toLowerCase().contains("delay"))
				.count();

		if (extremos > 0 && retrasados > 3) {
			return String.format("""
            🚨 ALERTA: %d climas extremos y %d vuelos retrasados detectados.
            Riesgo elevado de congestión aérea.
            """, extremos, retrasados);
		} else {
			return "✅ No se detectan riesgos combinados importantes en este momento.";
		}
	}
}
