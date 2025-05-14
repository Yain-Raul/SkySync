package com.skysync.application.ports.out;

import com.skysync.domain.model.Clima;

public interface ClimaPorCodigoPort {
	Clima obtenerClimaPorCodigo(String codigoIATA);
}