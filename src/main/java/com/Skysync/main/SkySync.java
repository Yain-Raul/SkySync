package com.Skysync.main;

import com.Skysync.core.BackgroundCollector;
import com.Skysync.core.DataCollector;
import com.Skysync.core.InformeGenerator;
import com.Skysync.core.PredictiveEngine;
import com.Skysync.core.ClimaCollector;
import com.Skysync.store.EventStoreBuilder;
import com.Skysync.business.BusinessUnit;

import java.util.Scanner;

public class SkySync {

	private static BusinessUnit businessUnit = new BusinessUnit();

	public static void main(String[] args) throws InterruptedException {
		Scanner scanner = new Scanner(System.in);
		boolean salir = false;

		System.out.println("🌤️ Bienvenido a SkySync\n");

		while (!salir) {
			System.out.println("\n1️⃣ Recolectar vuelos y clima actuales con AviationStack");
			System.out.println("2️⃣ Generar informe de un día");
			System.out.println("3️⃣ Predecir probabilidad de cancelación por clima");
			System.out.println("4️⃣ Recolección continua de vuelos en segundo plano");
			System.out.println("5️⃣ Iniciar Event Store Builder (modo escucha)");
			System.out.println("6️⃣ Iniciar Business Unit (procesamiento de clima en tiempo real)");
			System.out.println("7️⃣ Ver resumen de clima promedio");
			System.out.println("8️⃣ Detectar condiciones meteorológicas extremas");
			System.out.println("9️⃣ Ver estado de vuelos (retrasados y cancelados)");
			System.out.println("🔟 Cargar eventos históricos en el datamart");
			System.out.println("1️⃣1️⃣ Detectar alerta combinada clima + vuelos");
			System.out.println("0️⃣ Salir");
			System.out.print("\nElige una opción: ");

			int opcion = scanner.nextInt();
			scanner.nextLine(); // limpiar buffer

			switch (opcion) {
				case 1 -> {
					new DataCollector().recolectarVuelosPorAeropuerto();
					new ClimaCollector().recolectarClimaActual();
				}
				case 2 -> {
					System.out.print("Introduce fecha (YYYY-MM-DD): ");
					String fecha = scanner.nextLine();
					new InformeGenerator().generarResumenDelDia(fecha);
				}
				case 3 -> {
					System.out.print("Introduce el código de tu aeropuerto: ");
					String ciudad = scanner.nextLine();
					new PredictiveEngine().predecir(ciudad);
				}
				case 4 -> new BackgroundCollector().iniciarModoLento();
				case 5 -> {
					System.out.println("🚀 Lanzando Event Store Builder en segundo plano...");
					new Thread(() -> new EventStoreBuilder().iniciar()).start();
				}
				case 6 -> {
					System.out.println("🚀 Lanzando Business Unit en segundo plano...");
					new Thread(() -> businessUnit.iniciar()).start();
				}
				case 7 -> {
					businessUnit.mostrarResumen();
				}
				case 8 -> {
					businessUnit.detectarCondicionesExtremas();
				}
				case 9 -> {
					businessUnit.mostrarEstadoVuelos();
				}
				case 10 -> {
					businessUnit.cargarEventosHistoricos();
				}
				case 11 -> {
					businessUnit.lanzarAlertaCombinada();
				}

				case 0 -> {
					System.out.println("👋 Hasta luego!");
					salir = true;
				}
				default -> System.out.println("❌ Opción no válida.");
			}
		}
	}
}
