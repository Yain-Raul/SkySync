package com.skysync.application.ports.out;

import com.skysync.domain.model.Vuelo;
import java.util.List;

public interface VueloRepository {
	void guardar(Vuelo vuelo);
	List<Vuelo> obtenerPorFecha(String fecha);
	List<Vuelo> obtenerTodos();
}