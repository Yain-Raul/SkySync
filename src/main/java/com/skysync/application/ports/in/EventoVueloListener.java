package com.skysync.application.ports.in;

import com.skysync.domain.events.FlightEvent;

public interface EventoVueloListener {
	void procesar(FlightEvent evento);
}
