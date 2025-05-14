package com.skysync.application.services;

import com.google.gson.Gson;
import com.skysync.application.ports.in.CargarEventosHistoricosUseCase;
import com.skysync.application.ports.out.ClimaRepository;
import com.skysync.application.ports.out.VueloRepository;
import com.skysync.domain.events.FlightEvent;
import com.skysync.domain.events.WeatherEvent;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.List;
import java.util.ArrayList;

public class CargarEventosHistoricosService implements CargarEventosHistoricosUseCase {

	private final ClimaRepository climaRepo;
	private final VueloRepository vueloRepo;
	private final Gson gson = new Gson();

	public CargarEventosHistoricosService(ClimaRepository climaRepo, VueloRepository vueloRepo) {
		this.climaRepo = climaRepo;
		this.vueloRepo = vueloRepo;
	}

	@Override
	public void cargar() {
		System.out.println("\n📂 Cargando eventos históricos desde archivos...");
		List<File> archivos = listarArchivos("eventstore/prediction.Weather/feederA");
		archivos.addAll(listarArchivos("eventstore/prediction.Flight/feederB"));

		for (File archivo : archivos) {
			try (BufferedReader br = new BufferedReader(new FileReader(archivo))) {
				String linea;
				while ((linea = br.readLine()) != null) {
					procesarLinea(linea);
				}
			} catch (Exception e) {
				System.out.println("❌ Error leyendo archivo " + archivo.getName());
				e.printStackTrace();
			}
		}

		System.out.println("✅ Carga histórica completada.");
	}

	private void procesarLinea(String json) {
		try {
			if (json.contains("temperatura") && json.contains("humedad")) {
				WeatherEvent evento = gson.fromJson(json, WeatherEvent.class);
				climaRepo.guardar(evento.getData());
			} else if (json.contains("numeroVuelo") && json.contains("estado")) {
				FlightEvent evento = gson.fromJson(json, FlightEvent.class);
				vueloRepo.guardar(evento.getData());
			}
		} catch (Exception e) {
			System.out.println("⚠️ Error procesando evento: " + json);
			e.printStackTrace();
		}
	}

	private List<File> listarArchivos(String ruta) {
		List<File> archivos = new ArrayList<>();
		File carpeta = new File(ruta);
		if (carpeta.exists() && carpeta.isDirectory()) {
			File[] encontrados = carpeta.listFiles((dir, name) -> name.endsWith(".events"));
			if (encontrados != null) archivos.addAll(List.of(encontrados));
		}
		return archivos;
	}
}
