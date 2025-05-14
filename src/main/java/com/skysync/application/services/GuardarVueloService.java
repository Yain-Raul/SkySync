package com.skysync.application.services;

import com.skysync.application.ports.in.EventoVueloListener;
import com.skysync.application.ports.out.VueloRepository;
import com.skysync.domain.events.FlightEvent;

public class GuardarVueloService implements EventoVueloListener {

	private final VueloRepository repo;

	public GuardarVueloService(VueloRepository repo) {
		this.repo = repo;
	}

	@Override
	public void procesar(FlightEvent evento) {
		repo.guardar(evento.getData());
		System.out.println("✅ Vuelo procesado desde evento: " + evento.getData().getNumeroVuelo());
	}
}
