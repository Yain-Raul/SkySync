package com.skysync.adapters.out.persistence;

import com.skysync.application.ports.out.GuardarEventoPort;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.nio.file.Paths;

public class FileEventStoreAdapter implements GuardarEventoPort {

	private static final String BASE_DIR = "eventstore";

	@Override
	public void guardarEvento(String topic, String ss, String timestamp, String json) {
		try {
			String fecha = timestamp.substring(0, 10).replace("-", "");
			String ruta = Paths.get(BASE_DIR, topic, ss).toString();

			File dir = new File(ruta);
			if (!dir.exists()) dir.mkdirs();

			File archivo = new File(dir, fecha + ".events");
			try (PrintWriter pw = new PrintWriter(new FileWriter(archivo, true))) {
				pw.println(json);
			}
		} catch (Exception e) {
			System.out.println("❌ Error al guardar evento:");
			e.printStackTrace();
		}
	}
}
