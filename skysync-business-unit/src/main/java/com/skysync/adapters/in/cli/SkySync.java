package com.skysync.adapters.in.cli;

import com.skysync.adapters.in.messaging.BusinessUnitEventAdapter;
import com.skysync.adapters.out.persistence.SQLiteClimaRepository;
import com.skysync.adapters.out.persistence.SQLiteVueloRepository;
import com.skysync.core.aplication.ports.in.CargarEventosHistoricosUseCase;
import com.skysync.application.services.*;

import java.util.Scanner;

public class SkySync {

	public static void main(String[] args) {
		Scanner scanner = new Scanner(System.in);
		boolean salir = false;

		System.out.println("\n🌤️ Bienvenido a SkySync – Sistema de Análisis de Vuelos y Clima en Canarias\n");

		while (!salir) {
			mostrarMenu();

			int opcion = scanner.nextInt();
			scanner.nextLine(); // Limpiar buffer

			switch (opcion) {
				case 2 -> {
					System.out.print("\n📝 Introduce fecha para generar informe (YYYY-MM-DD): ");
					String fecha = scanner.nextLine();

					var climaRepo = new SQLiteClimaRepository();
					var vueloRepo = new SQLiteVueloRepository();
					var generarInforme = new GenerarInformeService(climaRepo, vueloRepo);

					String informe = generarInforme.generarInforme(fecha);
					System.out.println(informe);
				}

				case 3 -> {
					System.out.println("⚠️ Esta funcionalidad está disponible desde el módulo REST.");
					System.out.println("👉 Usa la interfaz web o el endpoint /prediccion?codigo=LPA");
				}


				case 4 -> {
					System.out.println("\n⚠️ La recolección continua se realiza desde:");
					System.out.println("   - skysync-feeder-weather");
					System.out.println("   - skysync-feeder-flights");
					System.out.println("👉 Ejecuta sus respectivos Main.java");
				}

				case 5 -> {
					System.out.println("\n⚠️ El almacenamiento de eventos se lanza desde:");
					System.out.println("   - skysync-event-store-builder");
					System.out.println("👉 Ejecuta su Main.java");
				}

				case 6 -> {
					System.out.println("\n📡 Iniciando procesamiento en tiempo real (BusinessUnit)...");
					var climaRepo = new SQLiteClimaRepository();
					var vueloRepo = new SQLiteVueloRepository();
					var business = new BusinessUnitEventAdapter(
							new GuardarClimaService(climaRepo),
							new GuardarVueloService(vueloRepo)
					);
					new Thread(business::iniciar).start();
				}

				case 7 -> {
					var servicio = new GenerarResumenClimaService(new SQLiteClimaRepository());
					System.out.println(servicio.generarResumen());
				}

				case 8 -> {
					var servicio = new DetectarCondicionesExtremasService(new SQLiteClimaRepository());
					System.out.println(servicio.detectar());
				}

				case 9 -> {
					var servicio = new ConsultarEstadoVuelosService(new SQLiteVueloRepository());
					System.out.println(servicio.obtenerEstado());
				}

				case 10 -> {
					System.out.println("\n📂 Cargando eventos históricos...");
					CargarEventosHistoricosUseCase servicio = new CargarEventosHistoricosService(
							new SQLiteClimaRepository(),
							new SQLiteVueloRepository()
					);
					servicio.cargar();
				}

				case 11 -> {
					var servicio = new DetectarAlertaCombinadaService(
							new SQLiteClimaRepository(),
							new SQLiteVueloRepository()
					);
					System.out.println(servicio.evaluar());
				}

				case 0 -> {
					System.out.println("\n👋 Cerrando SkySync. ¡Hasta pronto!");
					salir = true;
				}

				default -> System.out.println("\n❌ Opción no válida.");
			}
		}
	}

	private static void mostrarMenu() {
		System.out.println("\n==============================================");
		System.out.println("                 📋 MENÚ PRINCIPAL              ");
		System.out.println("==============================================");

		System.out.println("\n📊 Análisis y predicción:");
		System.out.println("  2️⃣ Generar informe de un día (clima y vuelos)");
		System.out.println("  3️⃣ Predecir probabilidad de cancelación por clima");
		System.out.println("  7️⃣ Ver resumen de clima promedio por ciudad");
		System.out.println("  8️⃣ Detectar condiciones meteorológicas extremas");
		System.out.println("  9️⃣ Ver estado de vuelos (retrasados y cancelados)");
		System.out.println("  1️⃣1️⃣ Detectar alerta combinada clima + vuelos");

		System.out.println("\n🗂️ Gestión y procesamiento:");
		System.out.println("  4️⃣ Aviso: recolección continua (feeder-weather / feeder-flights)");
		System.out.println("  5️⃣ Aviso: event-store se lanza desde su módulo");
		System.out.println("  6️⃣ Iniciar procesamiento en tiempo real (BusinessUnit)");
		System.out.println("  🔟 Cargar eventos históricos en el datamart");

		System.out.println("\n🚪 Salir:");
		System.out.println("  0️⃣ Salir");

		System.out.print("\n➡️ Elige una opción: ");
	}
}
