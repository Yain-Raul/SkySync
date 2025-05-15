package com.skysync.core.aplication.ports.out;

import com.skysync.core.domain.model.Clima;


public interface ClimaPorCodigoPort {
	Clima obtenerClimaPorCodigo(String codigoIATA);
}