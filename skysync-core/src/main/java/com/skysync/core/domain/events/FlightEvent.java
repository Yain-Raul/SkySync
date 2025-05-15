package com.skysync.core.domain.events;

import com.skysync.core.domain.model.Vuelo;
import java.time.Instant;

public class FlightEvent {
	private String ts;   // Timestamp en UTC
	private String ss;   // Source (ej. "feederB")
	private Vuelo data;  // Datos del vuelo

	public FlightEvent(String ss, Vuelo data) {
		this.ts = Instant.now().toString();
		this.ss = ss;
		this.data = data;
	}

	public String getTs() {
		return ts;
	}

	public String getSs() {
		return ss;
	}

	public Vuelo getData() {
		return data;
	}
}
