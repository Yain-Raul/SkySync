package com.skysync.adapters.out.persistence;

import com.skysync.application.ports.out.VueloRepository;
import com.skysync.domain.model.Vuelo;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SQLiteVueloRepository implements VueloRepository {

	private static final String DB_URL = "jdbc:sqlite:datamart.db";

	public SQLiteVueloRepository() {
		crearTablaSiNoExiste();
	}

	private Connection connect() throws SQLException {
		return DriverManager.getConnection(DB_URL);
	}

	private void crearTablaSiNoExiste() {
		String sql = """
            CREATE TABLE IF NOT EXISTS vuelos_datamart (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                numeroVuelo TEXT,
                aerolinea TEXT,
                aeropuertoSalida TEXT,
                aeropuertoLlegada TEXT,
                estado TEXT,
                timestamp TEXT
            );
        """;
		try (Connection conn = connect(); Statement stmt = conn.createStatement()) {
			stmt.execute(sql);
		} catch (SQLException e) {
			System.out.println("❌ Error creando tabla vuelos_datamart:");
			e.printStackTrace();
		}
	}

	@Override
	public List<Vuelo> obtenerPorFecha(String fecha) {
		List<Vuelo> vuelos = new ArrayList<>();
		String sql = """
            SELECT numeroVuelo, aerolinea, aeropuertoSalida, aeropuertoLlegada, estado
            FROM vuelos_datamart
            WHERE DATE(timestamp) = DATE(?);
        """;

		try (Connection conn = connect();
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

		} catch (SQLException e) {
			System.out.println("❌ Error leyendo vuelos por fecha:");
			e.printStackTrace();
		}

		return vuelos;
	}

	public void guardar(Vuelo vuelo) {
		String sql = """
            INSERT INTO vuelos_datamart (numeroVuelo, aerolinea, aeropuertoSalida, aeropuertoLlegada, estado, timestamp)
            VALUES (?, ?, ?, ?, ?, datetime('now'));
        """;

		try (Connection conn = connect(); PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setString(1, vuelo.getNumeroVuelo());
			stmt.setString(2, vuelo.getAerolinea());
			stmt.setString(3, vuelo.getAeropuertoSalida());
			stmt.setString(4, vuelo.getAeropuertoLlegada());
			stmt.setString(5, vuelo.getEstado());
			stmt.executeUpdate();
			System.out.println("📥 Vuelo insertado en datamart: " + vuelo.getNumeroVuelo());
		} catch (SQLException e) {
			System.out.println("❌ Error insertando vuelo:");
			e.printStackTrace();
		}
	}
	@Override
	public List<Vuelo> obtenerTodos() {
		List<Vuelo> vuelos = new ArrayList<>();
		String sql = "SELECT numeroVuelo, aerolinea, aeropuertoSalida, aeropuertoLlegada, estado FROM vuelos_datamart";

		try (Connection conn = connect();
			 PreparedStatement stmt = conn.prepareStatement(sql);
			 ResultSet rs = stmt.executeQuery()) {

			while (rs.next()) {
				Vuelo vuelo = new Vuelo();
				vuelo.setNumeroVuelo(rs.getString("numeroVuelo"));
				vuelo.setAerolinea(rs.getString("aerolinea"));
				vuelo.setAeropuertoSalida(rs.getString("aeropuertoSalida"));
				vuelo.setAeropuertoLlegada(rs.getString("aeropuertoLlegada"));
				vuelo.setEstado(rs.getString("estado"));
				vuelos.add(vuelo);
			}

		} catch (SQLException e) {
			System.out.println("❌ Error obteniendo todos los vuelos:");
			e.printStackTrace();
		}

		return vuelos;
	}

}
