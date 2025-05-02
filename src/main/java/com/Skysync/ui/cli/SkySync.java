package com.Skysync.ui.cli;

import com.Skysync.core.BackgroundCollector;
import com.Skysync.feeders.flights.DataCollector;
import com.Skysync.core.InformeGenerator;
import com.Skysync.core.PredictiveEngine;
import com.Skysync.feeders.weather.ClimaCollector;
import com.Skysync.store.EventStoreBuilder;
import com.Skysync.business.BusinessUnit;

import java.util.Scanner;

public class SkySync {

	private static final BusinessUnit businessUnit = new BusinessUnit();

	public static void main(String[] args) throws InterruptedException {
		Scanner scanner = new Scanner(System.in);
		boolean salir = false;

		System.out.println("\n🌤️ Bienvenido a SkySync – Sistema de Análisis de Vuelos y Clima en Canarias\n");

		while (!salir) {
			mostrarMenu();

			int opcion = scanner.nextInt();
			scanner.nextLine(); // Limpiar buffer

			switch (opcion) {
				case 1 -> {
					System.out.println("\n📥 Recolectando vuelos y clima actuales en Canarias...");
					new DataCollector().recolectarVuelosPorAeropuerto();
					new ClimaCollector().recolectarClimaActual();
				}
				case 2 -> {
					System.out.print("\n📝 Introduce fecha para generar informe (YYYY-MM-DD): ");
					String fecha = scanner.nextLine();
					new InformeGenerator().generarResumenDelDia(fecha);
				}
				case 3 -> {
					System.out.print("\n🔎 Introduce el código de tu aeropuerto (LPA, TFN, TFS, ACE, FUE, GMZ, SPC, VDE): ");
					String ciudad = scanner.nextLine();
					new PredictiveEngine().predecir(ciudad);
				}
				case 4 -> {
					System.out.println("\n♻️ Iniciando recolección continua de vuelos...");
					new BackgroundCollector().iniciarModoLento();
				}
				case 5 -> {
					System.out.println("\n📥 Iniciando EventStoreBuilder...");
					new Thread(() -> new EventStoreBuilder().iniciar()).start();
				}
				case 6 -> {
					System.out.println("\n📈 Iniciando BusinessUnit (procesamiento de clima y vuelos)...");
					new Thread(() -> businessUnit.iniciar()).start();
				}
				case 7 -> {
					System.out.println("\n📊 Mostrando resumen de clima promedio por ciudad...");
					businessUnit.mostrarResumen();
				}
				case 8 -> {
					System.out.println("\n🌩️ Buscando condiciones meteorológicas extremas...");
					businessUnit.detectarCondicionesExtremas();
				}
				case 9 -> {
					System.out.println("\n🛬 Mostrando estado de vuelos (retrasados y cancelados)...");
					businessUnit.mostrarEstadoVuelos();
				}
				case 10 -> {
					System.out.println("\n📂 Cargando eventos históricos al datamart...");
					businessUnit.cargarEventosHistoricos();
				}
				case 11 -> {
					System.out.println("\n🚨 Evaluando alerta combinada de clima extremo + vuelos retrasados...");
					businessUnit.lanzarAlertaCombinada();
				}
				case 0 -> {
					System.out.println("\n👋 Cerrando SkySync. ¡Hasta pronto!");
					salir = true;
				}
				default -> {
					System.out.println("\n❌ Opción no válida. Por favor, selecciona una opción del menú.");
				}
			}
		}
	}

	private static void mostrarMenu() {
		System.out.println("\n==============================================");
		System.out.println("                 📋 MENÚ PRINCIPAL              ");
		System.out.println("==============================================");

		System.out.println("\n📥 Recolección de datos:");
		System.out.println("  1️⃣ Recolectar vuelos y clima actuales con APIs");
		System.out.println("  4️⃣ Recolección continua de vuelos en segundo plano");
		System.out.println("  5️⃣ Iniciar EventStoreBuilder (almacenamiento de eventos)");
		System.out.println("  6️⃣ Iniciar BusinessUnit (procesamiento en tiempo real)");

		System.out.println("\n📊 Análisis y predicción:");
		System.out.println("  2️⃣ Generar informe de un día (clima y vuelos)");
		System.out.println("  3️⃣ Predecir probabilidad de cancelación por clima");
		System.out.println("  7️⃣ Ver resumen de clima promedio por ciudad");
		System.out.println("  8️⃣ Detectar condiciones meteorológicas extremas");
		System.out.println("  9️⃣ Ver estado de vuelos (retrasados y cancelados)");
		System.out.println("  1️⃣1️⃣ Detectar alerta combinada clima + vuelos");

		System.out.println("\n🗂️ Gestión de históricos:");
		System.out.println("  🔟 Cargar eventos históricos en el datamart");

		System.out.println("\n🚪 Salir:");
		System.out.println("  0️⃣ Salir");

		System.out.print("\n➡️ Elige una opción: ");
	}
}
