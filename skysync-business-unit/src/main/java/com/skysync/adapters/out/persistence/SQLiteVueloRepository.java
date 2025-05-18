package com.skysync.adapters.out.persistence;

import com.skysync.core.aplication.ports.out.VueloRepository;
import com.skysync.core.domain.model.Vuelo;

import java.sql.*;
import java.util.ArrayList; // Explicit import
import java.util.List;

public class SQLiteVueloRepository implements VueloRepository {

	private static final String DB_URL = "jdbc:sqlite:datamart.db";

	public SQLiteVueloRepository() {
		crearTablaSiNoExiste();
		limpiarFechasInvalidas();
	}

	private Connection connect() throws SQLException {
		return DriverManager.getConnection(DB_URL);
	}

	private void crearTablaSiNoExiste() {
		String sql = """
            CREATE TABLE IF NOT EXISTS vuelos_datamart (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                numeroVuelo TEXT NOT NULL,
                aerolinea TEXT NOT NULL,
                aeropuertoSalida TEXT NOT NULL,
                aeropuertoSalidaIATA TEXT NOT NULL,
                aeropuertoLlegada TEXT NOT NULL,
                estado TEXT NOT NULL,
                fecha TEXT NOT NULL,
                razonRetraso TEXT,
                UNIQUE(numeroVuelo, fecha, aeropuertoSalidaIATA, aeropuertoLlegada)
            );
        """;
		try (Connection conn = connect(); Statement stmt = conn.createStatement()) {
			stmt.execute(sql);
		} catch (SQLException e) {
			System.out.println("❌ Error creando tabla vuelos_datamart:");
			e.printStackTrace();
		}
	}

	private void limpiarFechasInvalidas() {
		String sql = "DELETE FROM vuelos_datamart WHERE fecha IS NULL OR fecha = '' OR fecha NOT LIKE '____-__-__'";
		try (Connection conn = connect(); Statement stmt = conn.createStatement()) {
			int rowsAffected = stmt.executeUpdate(sql);
			if (rowsAffected > 0) {
				System.out.println("🧹 Limpiados " + rowsAffected + " registros con fechas inválidas en vuelos_datamart");
			}
		} catch (SQLException e) {
			System.out.println("❌ Error limpiando fechas inválidas:");
			e.printStackTrace();
		}
	}

	@Override
	public List<Vuelo> obtenerPorFecha(String fecha) {
		List<Vuelo> vuelos = new ArrayList<>();
		String sql = """
            SELECT numeroVuelo, aerolinea, aeropuertoSalida, aeropuertoSalidaIATA, aeropuertoLlegada, estado, fecha, razonRetraso
            FROM vuelos_datamart
            WHERE DATE(fecha) = DATE(?);
        """;

		try (Connection conn = connect();
			 PreparedStatement stmt = conn.prepareStatement(sql)) {

			stmt.setString(1, fecha);
			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {
				Vuelo vuelo = new Vuelo(
						rs.getString("numeroVuelo"),
						rs.getString("aerolinea"),
						rs.getString("aeropuertoSalida"),
						rs.getString("aeropuertoSalidaIATA"),
						rs.getString("aeropuertoLlegada"),
						rs.getString("estado"),
						rs.getString("fecha"),
						rs.getString("razonRetraso")
				);
				vuelos.add(vuelo);
			}

		} catch (SQLException e) {
			System.out.println("❌ Error leyendo vuelos por fecha:");
			e.printStackTrace();
		}

		return vuelos;
	}

	public void guardar(Vuelo vuelo) {
		if (vuelo.getFecha() == null || vuelo.getFecha().isEmpty() || !vuelo.getFecha().matches("\\d{4}-\\d{2}-\\d{2}")) {
			throw new IllegalArgumentException("Flight date must be valid and in YYYY-MM-DD format, got: " + vuelo.getFecha());
		}

		String sql = """
            INSERT OR IGNORE INTO vuelos_datamart (numeroVuelo, aerolinea, aeropuertoSalida, aeropuertoSalidaIATA, aeropuertoLlegada, estado, fecha, razonRetraso)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?);
        """;

		try (Connection conn = connect(); PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setString(1, vuelo.getNumeroVuelo());
			stmt.setString(2, vuelo.getAerolinea());
			stmt.setString(3, vuelo.getAeropuertoSalida());
			stmt.setString(4, vuelo.getAeropuertoSalidaIATA());
			stmt.setString(5, vuelo.getAeropuertoLlegada());
			stmt.setString(6, vuelo.getEstado());
			stmt.setString(7, vuelo.getFecha());
			stmt.setString(8, vuelo.getRazonRetraso());
			int rowsAffected = stmt.executeUpdate();
			if (rowsAffected > 0) {
				System.out.println("📥 Vuelo insertado en datamart: " + vuelo.getNumeroVuelo());
			} else {
				System.out.println("ℹ️ Vuelo ya existe en datamart: " + vuelo.getNumeroVuelo());
			}
		} catch (SQLException e) {
			System.out.println("❌ Error insertando vuelo:");
			e.printStackTrace();
		}
	}

	@Override
	public List<Vuelo> obtenerTodos() {
		List<Vuelo> vuelos = new ArrayList<>();
		String sql = """
            SELECT numeroVuelo, aerolinea, aeropuertoSalida, aeropuertoSalidaIATA, aeropuertoLlegada, estado, fecha, razonRetraso 
            FROM vuelos_datamart 
            WHERE fecha IS NOT NULL AND fecha LIKE '____-__-__'
        """;

		try (Connection conn = connect();
			 PreparedStatement stmt = conn.prepareStatement(sql);
			 ResultSet rs = stmt.executeQuery()) {

			while (rs.next()) {
				Vuelo vuelo = new Vuelo(
						rs.getString("numeroVuelo"),
						rs.getString("aerolinea"),
						rs.getString("aeropuertoSalida"),
						rs.getString("aeropuertoSalidaIATA"),
						rs.getString("aeropuertoLlegada"),
						rs.getString("estado"),
						rs.getString("fecha"),
						rs.getString("razonRetraso")
				);
				vuelos.add(vuelo);
			}

		} catch (SQLException e) {
			System.out.println("❌ Error obteniendo todos los vuelos:");
			e.printStackTrace();
		}

		return vuelos;
	}
}