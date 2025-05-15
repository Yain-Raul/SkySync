package com.skysync.core.aplication.ports.out;

import com.skysync.core.domain.model.Clima;
import java.util.List;

public interface ClimaRepository {
	void guardar(Clima clima);
	List<Clima> obtenerPorFecha(String fecha);
	List<Clima> obtenerTodos();

}