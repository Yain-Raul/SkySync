package com.skysync.application.ports.out;

public interface GuardarEventoPort {
	void guardarEvento(String topic, String ss, String timestamp, String json);
}
