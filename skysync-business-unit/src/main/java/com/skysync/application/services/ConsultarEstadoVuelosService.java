package com.skysync.application.services;

import com.skysync.core.aplication.ports.out.VueloRepository;
import com.skysync.core.domain.model.Vuelo;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList; // Explicit import
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ConsultarEstadoVuelosService {

	private final VueloRepository repo;

	public ConsultarEstadoVuelosService(VueloRepository repo) {
		this.repo = repo;
	}

	public Map<String, Object> obtenerEstado(String fromDate, String toDate, String iata, int limit, int offset) {
		// Parse dates, default to today
		LocalDate today = LocalDate.now();
		LocalDate from = fromDate != null && isValidDate(fromDate) ? LocalDate.parse(fromDate) : today;
		LocalDate to = toDate != null && isValidDate(toDate) ? LocalDate.parse(toDate) : today;

		// Fetch flights within date range
		List<Vuelo> vuelos = repo.obtenerTodos().stream()
				.filter(v -> {
					try {
						if (v.getFecha() == null || v.getFecha().isEmpty()) {
							return false;
						}
						LocalDate vueloDate = LocalDate.parse(v.getFecha(), DateTimeFormatter.ISO_LOCAL_DATE);
						return !vueloDate.isBefore(from) && !vueloDate.isAfter(to);
					} catch (DateTimeParseException e) {
						System.out.println("❌ Invalid date format for flight " + v.getNumeroVuelo() + ": " + v.getFecha());
						return false;
					}
				})
				.filter(v -> iata == null || iata.isEmpty() || v.getAeropuertoSalidaIATA().equalsIgnoreCase(iata))
				.collect(Collectors.toList());

		// Apply pagination
		int total = vuelos.size();
		vuelos = vuelos.stream()
				.skip(offset)
				.limit(limit)
				.collect(Collectors.toList());

		// Group flights by airport
		Map<String, List<Vuelo>> vuelosPorAeropuerto = vuelos.stream()
				.collect(Collectors.groupingBy(Vuelo::getAeropuertoSalidaIATA));

		// Calculate stats
		List<Map<String, Object>> summary = vuelosPorAeropuerto.entrySet().stream()
				.map(entry -> {
					String aeropuertoIata = entry.getKey();
					List<Vuelo> vuelosAeropuerto = entry.getValue();

					long totalVuelos = vuelosAeropuerto.size();
					long retrasados = vuelosAeropuerto.stream()
							.filter(v -> v.getEstado().toLowerCase().contains("delay"))
							.count();
					long cancelados = vuelosAeropuerto.stream()
							.filter(v -> v.getEstado().toLowerCase().contains("cancel"))
							.count();
					long aTiempo = totalVuelos - retrasados - cancelados;

					Map<String, Long> razonesRetraso = vuelosAeropuerto.stream()
							.filter(v -> v.getEstado().toLowerCase().contains("delay") && v.getRazonRetraso() != null)
							.collect(Collectors.groupingBy(
									Vuelo::getRazonRetraso,
									Collectors.counting()
							));

					Map<String, Object> stats = new HashMap<>();
					stats.put("airportCode", aeropuertoIata);
					stats.put("totalFlights", totalVuelos);
					stats.put("onTime", aTiempo);
					stats.put("delayed", retrasados);
					stats.put("canceled", cancelados);
					stats.put("lastUpdated", LocalDate.now().toString());
					if (!razonesRetraso.isEmpty()) {
						stats.put("delayReasons", razonesRetraso);
					}

					return stats;
				})
				.sorted((a, b) -> ((String) a.get("airportCode")).compareTo((String) b.get("airportCode")))
				.collect(Collectors.toList());

		// Detailed flights list
		List<Map<String, String>> detailedFlights = vuelos.stream()
				.map(v -> {
					Map<String, String> flightDetails = new HashMap<>();
					flightDetails.put("flightNumber", v.getNumeroVuelo());
					flightDetails.put("airline", v.getAerolinea());
					flightDetails.put("departureAirport", v.getAeropuertoSalida());
					flightDetails.put("departureIATA", v.getAeropuertoSalidaIATA());
					flightDetails.put("arrivalAirport", v.getAeropuertoLlegada());
					flightDetails.put("status", v.getEstado());
					flightDetails.put("date", v.getFecha());
					if (v.getRazonRetraso() != null) {
						flightDetails.put("delayReason", v.getRazonRetraso());
					}
					return flightDetails;
				})
				.collect(Collectors.toList());

		// Prepare response
		Map<String, Object> data = new HashMap<>();
		data.put("summary", summary);
		data.put("flights", detailedFlights);
		data.put("dateRange", Map.of("from", from.toString(), "to", to.toString()));
		data.put("pagination", Map.of("limit", limit, "offset", offset, "total", total));
		data.put("links", Map.of(
				"self", String.format("/vuelos/estado?from=%s&to=%s&iata=%s&limit=%d&offset=%d",
						from, to, iata != null ? iata : "", limit, offset),
				"next", String.format("/vuelos/estado?from=%s&to=%s&iata=%s&limit=%d&offset=%d",
						from, to, iata != null ? iata : "", limit, offset + limit)
		));

		Map<String, Object> response = new HashMap<>();
		response.put("status", "success");
		response.put("data", data);
		return response;
	}

	public Map<String, Object> obtenerEstado() {
		return obtenerEstado(null, null, null, 10, 0);
	}

	private boolean isValidDate(String date) {
		if (date == null || date.isEmpty()) return false;
		try {
			LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE);
			return true;
		} catch (DateTimeParseException e) {
			return false;
		}
	}
}