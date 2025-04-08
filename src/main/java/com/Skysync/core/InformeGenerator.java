package com.Skysync.core;

import com.Skysync.database.DatabaseManager;
import com.Skysync.models.Clima;
import com.Skysync.models.Vuelo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class InformeGenerator {

	public void generarResumenDelDia(String fecha) {
		System.out.println("\n📊 Informe del día: " + fecha);

		try (Connection conn = DatabaseManager.connect()) {
			List<Vuelo> vuelos = obtenerVuelos(conn, fecha);
			List<Clima> climas = obtenerClimas(conn, fecha);

			long retrasos = vuelos.stream().filter(v -> v.getEstado().toLowerCase().contains("delay")).count();
			long cancelados = vuelos.stream().filter(v -> v.getEstado().toLowerCase().contains("cancel")).count();

			double tempMedia = climas.stream().mapToDouble(Clima::getTemperatura).average().orElse(0);
			double vientoMedio = climas.stream().mapToDouble(Clima::getVelocidadViento).average().orElse(0);
			double humedadMedia = climas.stream().mapToDouble(Clima::getHumedad).average().orElse(0);

			System.out.println("✈️ Total vuelos en Canarias: " + vuelos.size());
			System.out.println("   Retrasados en Canarias: " + retrasos + " | Cancelados: " + cancelados);
			System.out.printf("🌡️ Temperatura media en Canarias: %.1f°C | 💨 Viento: %.1f km/h | 💧 Humedad: %.0f%%\n",
					tempMedia, vientoMedio, humedadMedia);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private List<Vuelo> obtenerVuelos(Connection conn, String fecha) throws Exception {
		PreparedStatement stmt = conn.prepareStatement("SELECT * FROM vuelos WHERE DATE(fecha) = ?");
		stmt.setString(1, fecha);
		ResultSet rs = stmt.executeQuery();

		List<Vuelo> lista = new ArrayList<>();
		while (rs.next()) {
			lista.add(new Vuelo(
					rs.getString("numeroVuelo"),
					rs.getString("aerolinea"),
					rs.getString("aeropuertoSalida"),
					null,
					rs.getString("aeropuertoLlegada"),
					rs.getString("estado")
			));
		}
		return lista;
	}

	private List<Clima> obtenerClimas(Connection conn, String fecha) throws Exception {
		PreparedStatement stmt = conn.prepareStatement("SELECT * FROM clima WHERE DATE(fecha) = ?");
		stmt.setString(1, fecha);
		ResultSet rs = stmt.executeQuery();

		List<Clima> lista = new ArrayList<>();
		while (rs.next()) {
			lista.add(new Clima(
					rs.getString("ciudad"),
					rs.getDouble("temperatura"),
					rs.getDouble("humedad"),
					rs.getDouble("velocidadViento")
			));
		}
		return lista;
	}
}
