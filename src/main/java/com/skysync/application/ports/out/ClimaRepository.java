package com.skysync.application.ports.out;

import com.skysync.domain.model.Clima;
import java.util.List;

public interface ClimaRepository {
	void guardar(Clima clima);
	List<Clima> obtenerPorFecha(String fecha);
	List<Clima> obtenerTodos();

}