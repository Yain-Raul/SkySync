package com.skysync.application.services;

import com.skysync.core.aplication.ports.in.GenerarInformeUseCase;
import com.skysync.core.aplication.ports.out.ClimaRepository;
import com.skysync.core.aplication.ports.out.VueloRepository;
import com.skysync.core.domain.model.Clima;
import com.skysync.core.domain.model.Vuelo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class GenerarInformeService implements GenerarInformeUseCase {

	private final ClimaRepository climaRepo;
	private final VueloRepository vueloRepo;

	public GenerarInformeService(ClimaRepository climaRepo, VueloRepository vueloRepo) {
		this.climaRepo = climaRepo;
		this.vueloRepo = vueloRepo;
	}

	@Override
	public Map<String, Object> generarInforme(String fecha, String iata, String sort) {
		// Fetch data based on fecha and optional iata
		List<Vuelo> vuelos = vueloRepo.obtenerPorFecha(fecha);
		List<Clima> climas = climaRepo.obtenerPorFecha(fecha);

		// Filter by IATA if provided
		if (iata != null && !iata.isBlank()) {
			vuelos = vuelos.stream()
					.filter(v -> v.getAeropuertoSalidaIATA() != null && iata.equalsIgnoreCase(v.getAeropuertoSalidaIATA())) // Changed to getAeropuertoSalidaIATA
					.collect(Collectors.toList());
			climas = climas.stream()
					.filter(c -> c.getAirportCode() != null && iata.equalsIgnoreCase(c.getAirportCode())) // Using getAirportCode as added
					.collect(Collectors.toList());
		}

		// Sort vuelos if sort parameter is provided
		if (sort != null && !sort.isBlank()) {
			if ("delay_asc".equalsIgnoreCase(sort)) {
				vuelos.sort((v1, v2) -> {
					int delay1 = v1.getEstado().toLowerCase().contains("delay") ? 1 : 0;
					int delay2 = v2.getEstado().toLowerCase().contains("delay") ? 1 : 0;
					return Integer.compare(delay1, delay2);
				});
			} else if ("delay_desc".equalsIgnoreCase(sort)) {
				vuelos.sort((v1, v2) -> {
					int delay1 = v1.getEstado().toLowerCase().contains("delay") ? 1 : 0;
					int delay2 = v2.getEstado().toLowerCase().contains("delay") ? 1 : 0;
					return Integer.compare(delay2, delay1);
				});
			}
		}

		// Calculate statistics
		int totalVuelos = vuelos.size();
		int retrasados = (int) vuelos.stream().filter(v -> v.getEstado().toLowerCase().contains("delay")).count();
		int cancelados = (int) vuelos.stream().filter(v -> v.getEstado().toLowerCase().contains("cancel")).count();

		double tempMedia = climas.stream().mapToDouble(Clima::getTemperatura).average().orElse(0.0);
		double vientoMedia = climas.stream().mapToDouble(Clima::getVelocidadViento).average().orElse(0.0);
		double humedadMedia = climas.stream().mapToDouble(Clima::getHumedad).average().orElse(0.0);

		// Structure the data into a Map
		Map<String, Object> data = new HashMap<>();
		data.put("fecha", fecha);
		if (iata != null && !iata.isBlank()) data.put("iata", iata);
		if (sort != null && !sort.isBlank()) data.put("sort", sort);
		data.put("totalVuelos", totalVuelos);
		data.put("retrasados", retrasados);
		data.put("cancelados", cancelados);
		data.put("tempMedia", String.format("%.1f°C", tempMedia));
		data.put("vientoMedia", String.format("%.1f km/h", vientoMedia));
		data.put("humedadMedia", String.format("%.0f%%", humedadMedia));

		return data;
	}

	// Overload to maintain backward compatibility with the original interface
	public Map<String, Object> generarInforme(String fecha) {
		return generarInforme(fecha, null, null);
	}
}