package com.skysync.adapters.in.cli;

import com.skysync.adapters.in.messaging.BusinessUnitEventAdapter;
import com.skysync.adapters.in.messaging.EventStoreBrokerAdapter;
import com.skysync.adapters.in.scheduled.ScheduledDataCollector;
import com.skysync.adapters.out.api.OpenWeatherClimaAdapter;
import com.skysync.adapters.out.api.AviationStackAdapter;
import com.skysync.adapters.out.messaging.FlightPublisher;
import com.skysync.adapters.out.messaging.WeatherPublisher;
import com.skysync.adapters.out.persistence.SQLiteClimaRepository;
import com.skysync.adapters.out.persistence.SQLiteVueloRepository;
import com.skysync.application.ports.in.CargarEventosHistoricosUseCase;
import com.skysync.application.services.*;
import com.skysync.adapters.in.messaging.EventStoreBrokerAdapter;
import com.skysync.adapters.out.persistence.FileEventStoreAdapter;
import com.skysync.domain.events.FlightEvent;
import com.skysync.domain.events.WeatherEvent;
import com.skysync.domain.model.Clima;
import com.skysync.domain.model.Vuelo;

import java.util.List;
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
				case 1 -> {
					System.out.println("\n📥 Recolectando vuelos y clima actuales en Canarias...");

					var vuelosApi = new AviationStackAdapter();
					var climaApi = new OpenWeatherClimaAdapter();
					var flightPublisher = new FlightPublisher();
					var weatherPublisher = new WeatherPublisher();

					String[] aeropuertos = {"LPA", "TFN", "TFS", "ACE", "FUE", "GMZ", "SPC", "VDE"};
					String[] ciudades = {"Telde", "San Cristobal de la Laguna", "Granadilla de Abona", "Arrecife",
							"Puerto del Rosario", "Santa Cruz de la Palma", "Valverde", "Playa de Santiago"};

					for (String a : aeropuertos) {
						List<Vuelo> vuelos = vuelosApi.obtenerVuelosPorAeropuerto(a);
						vuelos.forEach(v -> flightPublisher.publicar(new FlightEvent("feederB", v)));
					}

					for (String c : ciudades) {
						Clima clima = climaApi.obtenerClima(c);
						if (clima != null) weatherPublisher.publicar(new WeatherEvent("feederA", clima));
					}
				}

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
					System.out.print("\n🔎 Código de aeropuerto (LPA, TFS, etc.): ");
					String codigo = scanner.nextLine();

					var climaPort = new OpenWeatherClimaAdapter();
					var servicio = new PredecirCancelacionService(climaPort);

					System.out.println(servicio.predecir(codigo));
				}

				case 4 -> {
					System.out.println("\n♻️ Iniciando recolección continua...");
					new ScheduledDataCollector().iniciarModoLento(); // mientras no sea multihilo
				}

				case 5 -> {
					System.out.println("\n💾 Iniciando almacenamiento de eventos (EventStore)...");
					var store = new EventStoreBrokerAdapter(new FileEventStoreAdapter());
					new Thread(store::iniciar).start();
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
