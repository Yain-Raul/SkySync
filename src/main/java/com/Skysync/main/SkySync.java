package com.Skysync.main;

import com.Skysync.core.BackgroundCollector;
import com.Skysync.core.DataCollector;
import com.Skysync.core.InformeGenerator;
import com.Skysync.core.PredictiveEngine;
import com.Skysync.core.ClimaCollector;

import java.util.Scanner;

public class SkySync {

	public static void main(String[] args) throws InterruptedException {
		Scanner scanner = new Scanner(System.in);
		System.out.println("🌤️ Bienvenido a SkySync\n");

		System.out.println("1️⃣ Recolectar vuelos actuales con AviationStack");
		System.out.println("2️⃣ Generar informe de un día");
		System.out.println("3️⃣ Predecir probabilidad de cancelación por clima");
		System.out.println("4️⃣ Recolección continua de vuelos en segundo plano");

		System.out.print("\nElige una opción: ");
		int opcion = scanner.nextInt();
		scanner.nextLine(); // limpiar buffer

		switch (opcion) {
			case 1 -> new DataCollector().recolectarVuelosPorAeropuerto();

			case 2 -> {
				System.out.print("Introduce fecha (YYYY-MM-DD): ");
				String fecha = scanner.nextLine();
				new InformeGenerator().generarResumenDelDia(fecha);
			}

			case 3 -> {
				System.out.print("Introduce ciudad canaria: ");
				String ciudad = scanner.nextLine();
				new PredictiveEngine().predecir(ciudad);
			}

			case 4 -> new BackgroundCollector().iniciarModoLento();

			default -> System.out.println("❌ Opción no válida.");

			case 5 -> new ClimaCollector().recolectarClimaActual();
		}


	}
}
