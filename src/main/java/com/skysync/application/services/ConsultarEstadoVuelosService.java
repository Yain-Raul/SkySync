package com.skysync.application.services;

import com.skysync.application.ports.out.VueloRepository;
import com.skysync.domain.model.Vuelo;

import java.util.List;

public class ConsultarEstadoVuelosService {

	private final VueloRepository repo;

	public ConsultarEstadoVuelosService(VueloRepository repo) {
		this.repo = repo;
	}

	public String obtenerEstado() {
		List<Vuelo> vuelos = repo.obtenerTodos();

		long retrasados = vuelos.stream().filter(v -> v.getEstado().toLowerCase().contains("delay")).count();
		long cancelados = vuelos.stream().filter(v -> v.getEstado().toLowerCase().contains("cancel")).count();

		return String.format("""
        🛬 Estado actual de vuelos:
        - ✈️ Vuelos retrasados: %d
        - 🛑 Vuelos cancelados: %d
        """, retrasados, cancelados);
	}
}
