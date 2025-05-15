package com.skysync.core.aplication.ports.in;

import com.skysync.core.domain.events.WeatherEvent;

public interface EventoClimaListener {
	void procesar(WeatherEvent evento);
}
