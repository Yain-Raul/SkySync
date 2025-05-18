package com.skysync.adapters.out.persistence;

import com.skysync.core.aplication.ports.out.ClimaRepository;
import com.skysync.core.domain.model.Clima;

import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class SQLiteClimaRepository implements ClimaRepository {

	private static final String DB_URL = "jdbc:sqlite:datamart.db";

	public SQLiteClimaRepository() {
		crearTablaSiNoExiste();
	}

	private Connection connect() throws SQLException {
		return DriverManager.getConnection(DB_URL);
	}

	private void crearTablaSiNoExiste() {
		String sql = """
            CREATE TABLE IF NOT EXISTS clima_datamart (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                ciudad TEXT NOT NULL,
                airportCode TEXT NOT NULL,
                temperatura REAL NOT NULL,
                humedad REAL NOT NULL,
                viento REAL NOT NULL,
                condicion TEXT NOT NULL,
                timestamp TEXT NOT NULL,
                UNIQUE(ciudad, airportCode, timestamp)
            );
        """;
		try (Connection conn = connect(); Statement stmt = conn.createStatement()) {
			stmt.execute(sql);
		} catch (SQLException e) {
			System.err.println("❌ Error creando tabla clima_datamart: " + e.getMessage());
		}
	}

	@Override
	public void guardar(Clima clima) {
		String sql = """
            INSERT OR IGNORE INTO clima_datamart (ciudad, airportCode, temperatura, humedad, viento, condicion, timestamp)
            VALUES (?, ?, ?, ?, ?, ?, ?);
        """;
		try (Connection conn = connect(); PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setString(1, clima.getCiudad());
			stmt.setString(2, clima.getAirportCode());
			stmt.setDouble(3, clima.getTemperatura());
			stmt.setDouble(4, clima.getHumedad());
			stmt.setDouble(5, clima.getVelocidadViento());
			stmt.setString(6, clima.getCondicion());
			stmt.setString(7, clima.getTimestamp().toString());
			int rowsAffected = stmt.executeUpdate();
			if (rowsAffected > 0) {
				System.out.println("📥 Clima insertado: " + clima.getCiudad());
			}
		} catch (SQLException e) {
			System.err.println("❌ Error insertando clima: " + e.getMessage());
		}
	}

	@Override
	public List<Clima> obtenerPorFecha(String fecha) {
		return null;
	}

	@Override
	public List<Clima> obtenerTodos() {
		List<Clima> climas = new ArrayList<>();
		String sql = """
            SELECT ciudad, airportCode, temperatura, humedad, viento, condicion, timestamp
            FROM clima_datamart;
        """;
		try (Connection conn = connect(); PreparedStatement stmt = conn.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {
			while (rs.next()) {
				Clima clima = new Clima();
				clima.setCiudad(rs.getString("ciudad"));
				clima.setAirportCode(rs.getString("airportCode"));
				clima.setTemperatura(rs.getDouble("temperatura"));
				clima.setHumedad(rs.getDouble("humedad"));
				clima.setVelocidadViento(rs.getDouble("viento"));
				clima.setCondicion(rs.getString("condicion"));
				String timestampStr = rs.getString("timestamp");
				clima.setTimestamp(timestampStr != null ? Instant.parse(timestampStr) : null);
				climas.add(clima);
			}
		} catch (SQLException e) {
			System.err.println("❌ Error obteniendo climas: " + e.getMessage());
		}
		return climas;
	}
}