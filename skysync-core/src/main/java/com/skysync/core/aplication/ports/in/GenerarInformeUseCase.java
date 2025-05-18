package com.skysync.core.aplication.ports.in;

import java.util.Map;

public interface GenerarInformeUseCase {
	Map<String, Object> generarInforme(String fecha, String iata, String sort);
}