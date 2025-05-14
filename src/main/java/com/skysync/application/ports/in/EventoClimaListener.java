package com.skysync.application.ports.in;

import com.skysync.domain.events.WeatherEvent;

public interface EventoClimaListener {
	void procesar(WeatherEvent evento);
}
