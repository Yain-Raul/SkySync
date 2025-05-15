package com.skysync.application.services;

import com.skysync.core.aplication.ports.in.EventoVueloListener;
import com.skysync.core.aplication.ports.out.VueloRepository;

import com.skysync.core.domain.events.FlightEvent;

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
