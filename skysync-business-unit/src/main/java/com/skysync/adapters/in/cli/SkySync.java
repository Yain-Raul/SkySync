package com.skysync.adapters.in.cli;

import com.skysync.adapters.in.messaging.BusinessUnitEventAdapter;
import com.skysync.adapters.out.persistence.SQLiteClimaRepository;
import com.skysync.adapters.out.persistence.SQLiteVueloRepository;
import com.skysync.core.aplication.ports.in.CargarEventosHistoricosUseCase;
import com.skysync.application.services.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import java.util.Map;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;
import java.util.regex.Pattern;

public class SkySync {

	private static final Logger logger = LoggerFactory.getLogger(SkySync.class);
	private static final Pattern DATE_PATTERN = Pattern.compile("\\d{4}-\\d{2}-\\d{2}");
	private static BusinessUnitEventAdapter businessUnit; // To manage the thread

	public static void main(String[] args) {
		try (Scanner scanner = new Scanner(System.in)) {
			boolean salir = false;

			System.out.println("\n🌤️ Bienvenido a SkySync – Sistema de Análisis de Vuelos y Clima en Canarias\n");

			while (!salir) {
				mostrarMenu();

				try {
					int opcion = scanner.nextInt();
					scanner.nextLine(); // Limpiar buffer

					switch (opcion) {
						case 2 -> {
							String currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
							System.out.printf("\n📝 Introduce fecha para generar informe (YYYY-MM-DD, ej: %s): ", currentDate);
							String fecha = scanner.nextLine();

							if (!isValidDate(fecha)) {
								System.out.println("⚠️ Formato de fecha inválido. Use YYYY-MM-DD, por ejemplo: " + currentDate);
								break;
							}

							var climaRepo = new SQLiteClimaRepository();
							var vueloRepo = new SQLiteVueloRepository();
							var generarInforme = new GenerarInformeService(climaRepo, vueloRepo);

							try {
								Map<String, Object> informeData = generarInforme.generarInforme(fecha);
								String informe = formatInforme(informeData);
								System.out.println("\n📊 Informe generado:");
								System.out.println(informe);
							} catch (Exception e) {
								logger.error("Error al generar informe: {}", e.getMessage(), e);
								System.out.println("⚠️ Error al generar informe: " + e.getMessage());
							}
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
							if (businessUnit != null && businessUnit.isRunning()) {
								System.out.println("⚠️ BusinessUnit ya está en ejecución.");
								break;
							}
							System.out.println("\n📡 Iniciando procesamiento en tiempo real (BusinessUnit)...");
							var climaRepo = new SQLiteClimaRepository();
							var vueloRepo = new SQLiteVueloRepository();
							businessUnit = new BusinessUnitEventAdapter(
									new GuardarClimaService(climaRepo),
									new GuardarVueloService(vueloRepo)
							);
							new Thread(businessUnit::iniciar).start();
						}

						case 7 -> {
							try {
								var servicio = new GenerarResumenClimaService(new SQLiteClimaRepository());
								Map<String, Object> resultado = servicio.generarResumen();

								// Extract the status and data
								if ("success".equals(resultado.get("status"))) {
									Map<String, Object> data = (Map<String, Object>) resultado.get("data");
									List<Map<String, Object>> summary = (List<Map<String, Object>>) data.get("summary");
									Map<String, String> dateRange = (Map<String, String>) data.get("dateRange");
									Map<String, Object> pagination = (Map<String, Object>) data.get("pagination");

									// Print the climate summary
									System.out.println("\n📈 Resumen de Clima Promedio:");
									if (summary.isEmpty()) {
										System.out.println("No hay datos climáticos disponibles.");
									} else {
										for (Map<String, Object> cityData : summary) {
											String city = (String) cityData.get("city");
											String airportCode = (String) cityData.get("airportCode");
											Map<String, String> averages = (Map<String, String>) cityData.get("averages");
											Integer recordCount = (Integer) cityData.get("recordCount");
											String lastUpdated = (String) cityData.get("lastUpdated");

											System.out.printf("- %s (%s) → 🌡️ %s, 💧 %s, 💨 %s%n",
													city, airportCode,
													averages.get("temperature"),
													averages.get("humidity"),
													averages.get("windSpeed"));
											System.out.printf("  Registros: %d, Última Actualización: %s%n",
													recordCount, lastUpdated);
										}

										// Print date range and pagination
										System.out.println("\nRango de Fechas:");
										System.out.printf("  Desde: %s, Hasta: %s%n",
												dateRange.get("from"), dateRange.get("to"));
										System.out.println("\nPaginación:");
										System.out.printf("  Total: %d, Límite: %d, Offset: %d%n",
												pagination.get("total"), pagination.get("limit"), pagination.get("offset"));
									}
								} else {
									System.out.println("⚠️ Error al generar resumen de clima: " + resultado.get("message"));
								}
							} catch (Exception e) {
								logger.error("Error al generar resumen de clima: {}", e.getMessage(), e);
								System.out.println("⚠️ Error al generar resumen de clima: " + e.getMessage());
							}
						}

						case 8 -> {
							try {
								var servicio = new DetectarCondicionesExtremasService(new SQLiteClimaRepository());
								String extremos = servicio.detectar();
								System.out.println("\n🌪️ Condiciones extremas detectadas:");
								System.out.println(extremos);
							} catch (Exception e) {
								logger.error("Error al detectar condiciones extremas: {}", e.getMessage(), e);
								System.out.println("⚠️ Error al detectar condiciones extremas: " + e.getMessage());
							}
						}

						case 9 -> {
							Logger logger = LoggerFactory.getLogger("SkySyncCLI");
							try {
								var vueloRepo = new SQLiteVueloRepository();
								var servicio = new ConsultarEstadoVuelosService(vueloRepo);
								Map<String, Object> resultado = servicio.obtenerEstado();

								if ("success".equals(resultado.get("status"))) {
									Map<String, Object> data = (Map<String, Object>) resultado.get("data");
									List<Map<String, Object>> summary = (List<Map<String, Object>>) data.get("summary");
									List<Map<String, String>> flights = (List<Map<String, String>>) data.get("flights");
									Map<String, String> dateRange = (Map<String, String>) data.get("dateRange");
									Map<String, Object> pagination = (Map<String, Object>) data.get("pagination");

									System.out.println("\n🛬 Estado de Vuelos (Hoy, " + dateRange.get("from") + "):");
									if (summary.isEmpty() && flights.isEmpty()) {
										System.out.println("No hay datos de vuelos disponibles para hoy.");
										if (resultado.containsKey("message")) {
											System.out.println("Mensaje: " + resultado.get("message"));
										}
									} else {
										for (Map<String, Object> airportData : summary) {
											String airportCode = (String) airportData.get("airportCode");
											Long totalFlights = ((Number) airportData.get("totalFlights")).longValue();
											Long onTime = ((Number) airportData.get("onTime")).longValue();
											Long delayed = ((Number) airportData.get("delayed")).longValue();
											Long canceled = ((Number) airportData.get("canceled")).longValue();
											String lastUpdated = (String) airportData.get("lastUpdated");
											Map<String, Long> delayReasons = (Map<String, Long>) airportData.get("delayReasons");

											System.out.printf("Aeropuerto: %s%n", airportCode);
											System.out.printf("  Total de Vuelos: %d%n", totalFlights);
											System.out.printf("  A Tiempo: %d%n", onTime);
											System.out.printf("  Retrasados: %d%n", delayed);
											System.out.printf("  Cancelados: %d%n", canceled);
											if (delayReasons != null && !delayReasons.isEmpty()) {
												System.out.println("  Razones de Retraso:");
												delayReasons.forEach((reason, count) ->
														System.out.printf("    - %s: %d vuelos%n", reason, count));
											}
											System.out.printf("  Última Actualización: %s%n%n", lastUpdated);
										}

										if (!flights.isEmpty()) {
											System.out.println("Vuelos Detallados:");
											for (Map<String, String> flight : flights) {
												System.out.printf("  Vuelo: %s%n", flight.get("flightNumber"));
												System.out.printf("    Aerolínea: %s%n", flight.get("airline"));
												System.out.printf("    Salida: %s (%s)%n", flight.get("departureAirport"), flight.get("departureIATA"));
												System.out.printf("    Llegada: %s%n", flight.get("arrivalAirport"));
												System.out.printf("    Estado: %s%n", flight.get("status"));
												System.out.printf("    Fecha: %s%n", flight.get("date"));
												if (flight.containsKey("delayReason")) {
													System.out.printf("    Razón de Retraso: %s%n", flight.get("delayReason"));
												}
												System.out.println();
											}
										}

										System.out.println("Rango de Fechas:");
										System.out.printf("  Desde: %s, Hasta: %s%n", dateRange.get("from"), dateRange.get("to"));
										System.out.println("Paginación:");
										System.out.printf("  Total: %d, Límite: %d, Offset: %d%n",
												((Number) pagination.get("total")).longValue(),
												((Number) pagination.get("limit")).longValue(),
												((Number) pagination.get("offset")).longValue());
									}
								} else {
									System.out.println("⚠️ Error al consultar estado de vuelos: " + resultado.get("message"));
								}
							} catch (Exception e) {
								logger.error("Error al consultar estado de vuelos: {}", e.getMessage(), e);
								System.out.println("⚠️ Error al consultar estado de vuelos: " + e.getMessage());
							}
						}

						case 10 -> {
							System.out.println("\n📂 Cargando eventos históricos...");
							try {
								CargarEventosHistoricosUseCase servicio = new CargarEventosHistoricosService(
										new SQLiteClimaRepository(),
										new SQLiteVueloRepository()
								);
								servicio.cargar();
								System.out.println("✅ Eventos históricos cargados exitosamente.");
							} catch (Exception e) {
								logger.error("Error al cargar eventos históricos: {}", e.getMessage(), e);
								System.out.println("⚠️ Error al cargar eventos históricos: " + e.getMessage());
							}
						}

						case 11 -> {
							try {
								var servicio = new DetectarAlertaCombinadaService(
										new SQLiteClimaRepository(),
										new SQLiteVueloRepository()
								);
								String alerta = servicio.evaluar();
								System.out.println("\n🚨 Alerta combinada clima + vuelos:");
								System.out.println(alerta);
							} catch (Exception e) {
								logger.error("Error al detectar alerta combinada: {}", e.getMessage(), e);
								System.out.println("⚠️ Error al detectar alerta combinada: " + e.getMessage());
							}
						}

						case 0 -> {
							System.out.println("\n👋 Cerrando SkySync. ¡Hasta pronto!");
							if (businessUnit != null && businessUnit.isRunning()) {
								businessUnit.stop(); // Stop the BusinessUnit thread
								try {
									Thread.sleep(1000); // Give it a second to shut down
								} catch (InterruptedException e) {
									logger.error("Interrupción al cerrar BusinessUnit: {}", e.getMessage(), e);
								}
							}
							salir = true;
						}

						default -> System.out.println("\n❌ Opción no válida.");
					}
				} catch (Exception e) {
					logger.error("Error al procesar la opción del menú: {}", e.getMessage(), e);
					System.out.println("\n❌ Error: Opción inválida o entrada no numérica. Por favor, introduce un número.");
					scanner.nextLine(); // Clear invalid input
				}
			}
		}
	}

	private static void mostrarMenu() {
		System.out.println("\n==============================================");
		System.out.println("                 📋 MENÚ PRINCIPAL              ");
		System.out.println("==============================================");

		System.out.println("\n📊 Análisis y predicción:");
		System.out.println("  1️⃣ [Reservado para futura funcionalidad]");
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

	private static boolean isValidDate(String fecha) {
		return fecha != null && DATE_PATTERN.matcher(fecha).matches();
	}

	private static String formatInforme(Map<String, Object> informeData) {
		StringBuilder sb = new StringBuilder();
		sb.append("Fecha: ").append(informeData.get("fecha")).append("\n");
		if (informeData.containsKey("iata")) {
			sb.append("IATA: ").append(informeData.get("iata")).append("\n");
		}
		if (informeData.containsKey("sort")) {
			sb.append("Ordenado por: ").append(informeData.get("sort")).append("\n");
		}
		sb.append("Total de vuelos: ").append(informeData.get("totalVuelos")).append("\n");
		sb.append("Vuelos retrasados: ").append(informeData.get("retrasados")).append("\n");
		sb.append("Vuelos cancelados: ").append(informeData.get("cancelados")).append("\n");
		sb.append("Temperatura media: ").append(informeData.get("tempMedia")).append("\n");
		sb.append("Velocidad del viento media: ").append(informeData.get("vientoMedia")).append("\n");
		sb.append("Humedad media: ").append(informeData.get("humedadMedia")).append("\n");
		return sb.toString();
	}
}