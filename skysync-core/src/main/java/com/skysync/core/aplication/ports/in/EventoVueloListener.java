package com.skysync.core.aplication.ports.in;


import com.skysync.core.domain.events.FlightEvent;
public interface EventoVueloListener {
	void procesar(FlightEvent evento);
}
