package com.skysync.application.services;

import com.skysync.application.ports.in.GenerarInformeUseCase;
import com.skysync.application.ports.out.ClimaRepository;
import com.skysync.application.ports.out.VueloRepository;
import com.skysync.domain.model.Clima;
import com.skysync.domain.model.Vuelo;

import java.util.List;

public class GenerarInformeService implements GenerarInformeUseCase {

	private final ClimaRepository climaRepo;
	private final VueloRepository vueloRepo;

	public GenerarInformeService(ClimaRepository climaRepo, VueloRepository vueloRepo) {
		this.climaRepo = climaRepo;
		this.vueloRepo = vueloRepo;
	}

	@Override
	public String generarInforme(String fecha) {
		List<Vuelo> vuelos = vueloRepo.obtenerPorFecha(fecha);
		List<Clima> climas = climaRepo.obtenerPorFecha(fecha);

		int totalVuelos = vuelos.size();
		int retrasados = (int) vuelos.stream().filter(v -> v.getEstado().toLowerCase().contains("delay")).count();
		int cancelados = (int) vuelos.stream().filter(v -> v.getEstado().toLowerCase().contains("cancel")).count();

		double temp = climas.stream().mapToDouble(Clima::getTemperatura).average().orElse(0.0);
		double viento = climas.stream().mapToDouble(Clima::getVelocidadViento).average().orElse(0.0);
		double humedad = climas.stream().mapToDouble(Clima::getHumedad).average().orElse(0.0);

		return String.format("""
                📅 Informe del día: %s
                ✈️ Total vuelos: %d | Retrasados: %d | Cancelados: %d
                🌡️ Temp media: %.1f°C | 💨 Viento medio: %.1f km/h | 💧 Humedad media: %.0f%%
                """, fecha, totalVuelos, retrasados, cancelados, temp, viento, humedad);
	}
}
