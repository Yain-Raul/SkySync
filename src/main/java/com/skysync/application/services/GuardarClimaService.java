package com.skysync.application.services;

import com.skysync.application.ports.in.EventoClimaListener;
import com.skysync.application.ports.out.ClimaRepository;
import com.skysync.domain.events.WeatherEvent;

public class GuardarClimaService implements EventoClimaListener {

	private final ClimaRepository repo;

	public GuardarClimaService(ClimaRepository repo) {
		this.repo = repo;
	}

	@Override
	public void procesar(WeatherEvent evento) {
		repo.guardar(evento.getData());
		System.out.println("✅ Clima procesado desde evento: " + evento.getData().getCiudad());
	}
}
