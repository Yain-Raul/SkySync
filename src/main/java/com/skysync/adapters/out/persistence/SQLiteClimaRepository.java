package com.skysync.adapters.out.persistence;

import com.skysync.application.ports.out.ClimaRepository;
import com.skysync.domain.model.Clima;

import java.sql.*;
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
                ciudad TEXT,
                temperatura REAL,
                humedad REAL,
                velocidadViento REAL,
                condicion TEXT,
                timestamp TEXT
            );
        """;
		try (Connection conn = connect(); Statement stmt = conn.createStatement()) {
			stmt.execute(sql);
		} catch (SQLException e) {
			System.out.println("❌ Error creando tabla clima_datamart:");
			e.printStackTrace();
		}
	}

	@Override
	public List<Clima> obtenerPorFecha(String fecha) {
		List<Clima> climas = new ArrayList<>();
		String sql = """
            SELECT ciudad, temperatura, humedad, velocidadViento, condicion
            FROM clima_datamart
            WHERE DATE(timestamp) = DATE(?);
        """;

		try (Connection conn = connect();
			 PreparedStatement stmt = conn.prepareStatement(sql)) {

			stmt.setString(1, fecha);
			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {
				Clima clima = new Clima();
				clima.setCiudad(rs.getString("ciudad"));
				clima.setTemperatura(rs.getDouble("temperatura"));
				clima.setHumedad(rs.getDouble("humedad"));
				clima.setVelocidadViento(rs.getDouble("velocidadViento"));
				clima.setCondicion(rs.getString("condicion"));
				climas.add(clima);
			}

		} catch (SQLException e) {
			System.out.println("❌ Error leyendo clima por fecha:");
			e.printStackTrace();
		}

		return climas;
	}

	public void guardar(Clima clima) {
		String sql = """
            INSERT INTO clima_datamart (ciudad, temperatura, humedad, velocidadViento, condicion, timestamp)
            VALUES (?, ?, ?, ?, ?, datetime('now'));
        """;

		try (Connection conn = connect(); PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setString(1, clima.getCiudad());
			stmt.setDouble(2, clima.getTemperatura());
			stmt.setDouble(3, clima.getHumedad());
			stmt.setDouble(4, clima.getVelocidadViento());
			stmt.setString(5, clima.getCondicion());
			stmt.executeUpdate();
			System.out.println("📥 Clima insertado en datamart: " + clima.getCiudad());
		} catch (SQLException e) {
			System.out.println("❌ Error insertando clima:");
			e.printStackTrace();
		}
	}
	@Override
	public List<Clima> obtenerTodos() {
		List<Clima> climas = new ArrayList<>();
		String sql = "SELECT ciudad, temperatura, humedad, velocidadViento, condicion FROM clima_datamart";

		try (Connection conn = connect();
			 PreparedStatement stmt = conn.prepareStatement(sql);
			 ResultSet rs = stmt.executeQuery()) {

			while (rs.next()) {
				Clima clima = new Clima();
				clima.setCiudad(rs.getString("ciudad"));
				clima.setTemperatura(rs.getDouble("temperatura"));
				clima.setHumedad(rs.getDouble("humedad"));
				clima.setVelocidadViento(rs.getDouble("velocidadViento"));
				clima.setCondicion(rs.getString("condicion"));
				climas.add(clima);
			}

		} catch (SQLException e) {
			System.out.println("❌ Error obteniendo todos los climas:");
			e.printStackTrace();
		}

		return climas;
	}

}
