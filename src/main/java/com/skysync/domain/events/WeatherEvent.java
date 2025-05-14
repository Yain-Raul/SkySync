package com.skysync.domain.events;

import com.skysync.domain.model.Clima;
import java.time.Instant;

public class WeatherEvent {
	private String ts;
	private String ss;
	private Clima data;

	public WeatherEvent(String ss, Clima data) {
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

	public Clima getData() {
		return data;
	}
}
