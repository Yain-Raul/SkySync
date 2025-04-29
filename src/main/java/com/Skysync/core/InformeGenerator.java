package com.Skysync.core;

import com.Skysync.models.Clima;
import com.Skysync.models.Vuelo;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class InformeGenerator {

	private static final String DB_URL = "jdbc:sqlite:datamart.db";

	public void generarResumenDelDia(String fecha) {
		List<Vuelo> vuelos = obtenerVuelos(fecha);
		List<Clima> climas = obtenerClimas(fecha);

		System.out.println("\n📅 Informe del día: " + fecha);

		// Vuelos
		int totalVuelos = vuelos.size();
		int retrasados = (int) vuelos.stream().filter(v -> v.getEstado().toLowerCase().contains("delay")).count();
		int cancelados = (int) vuelos.stream().filter(v -> v.getEstado().toLowerCase().contains("cancel")).count();

		System.out.println("✈️ Total vuelos en Canarias: " + totalVuelos);
		System.out.println("Retrasados: " + retrasados + " | Cancelados: " + cancelados);

		// Clima
		double temperaturaMedia = climas.stream().mapToDouble(Clima::getTemperatura).average().orElse(0.0);
		double vientoMedio = climas.stream().mapToDouble(Clima::getVelocidadViento).average().orElse(0.0);
		double humedadMedia = climas.stream().mapToDouble(Clima::getHumedad).average().orElse(0.0);

		System.out.printf("🌡️ Temperatura media: %.1f°C | 💨 Viento medio: %.1f km/h | 💧 Humedad media: %.0f%%%n",
				temperaturaMedia, vientoMedio, humedadMedia);
	}

	private List<Vuelo> obtenerVuelos(String fecha) {
		List<Vuelo> vuelos = new ArrayList<>();
		String sql = """
            SELECT numeroVuelo, aerolinea, aeropuertoSalida, aeropuertoLlegada, estado
            FROM vuelos_datamart
            WHERE DATE(timestamp) = DATE(?);
        """;

		try (Connection conn = DriverManager.getConnection(DB_URL);
			 PreparedStatement stmt = conn.prepareStatement(sql)) {

			stmt.setString(1, fecha);
			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {
				Vuelo vuelo = new Vuelo();
				vuelo.setNumeroVuelo(rs.getString("numeroVuelo"));
				vuelo.setAerolinea(rs.getString("aerolinea"));
				vuelo.setAeropuertoSalida(rs.getString("aeropuertoSalida"));
				vuelo.setAeropuertoLlegada(rs.getString("aeropuertoLlegada"));
				vuelo.setEstado(rs.getString("estado"));
				vuelos.add(vuelo);
			}

		} catch (Exception e) {
			System.out.println("❌ Error obteniendo vuelos:");
			e.printStackTrace();
		}

		return vuelos;
	}

	private List<Clima> obtenerClimas(String fecha) {
		List<Clima> climas = new ArrayList<>();
		String sql = """
            SELECT ciudad, temperatura, humedad, velocidadViento
            FROM clima_datamart
            WHERE DATE(timestamp) = DATE(?);
        """;

		try (Connection conn = DriverManager.getConnection(DB_URL);
			 PreparedStatement stmt = conn.prepareStatement(sql)) {

			stmt.setString(1, fecha);
			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {
				Clima clima = new Clima();
				clima.setCiudad(rs.getString("ciudad"));
				clima.setTemperatura(rs.getDouble("temperatura"));
				clima.setHumedad(rs.getDouble("humedad"));
				clima.setVelocidadViento(rs.getDouble("velocidadViento"));
				climas.add(clima);
			}

		} catch (Exception e) {
			System.out.println("❌ Error obteniendo climas:");
			e.printStackTrace();
		}

		return climas;
	}
}
