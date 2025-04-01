package com.Skysync.database;

import com.Skysync.models.Clima;
import com.Skysync.models.Vuelo;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DatabaseManager {
	private static final String URL = "jdbc:sqlite:clima_vuelos.db";


	public void crearTablas() {
		String sqlClima = """
        CREATE TABLE IF NOT EXISTS clima (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            ciudad TEXT NOT NULL,
            temperatura REAL,
            humedad REAL,
            velocidadViento REAL,
            fecha DATETIME DEFAULT CURRENT_TIMESTAMP
        );
    """;

		String sqlVuelos = """
        CREATE TABLE IF NOT EXISTS vuelos (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            numeroVuelo TEXT NOT NULL,
            aerolinea TEXT,
            aeropuertoSalida TEXT,
            aeropuertoLlegada TEXT,
            estado TEXT,
            fecha DATETIME DEFAULT CURRENT_TIMESTAMP
        );
    """;

		try (Connection conn = connect()) {
			if (conn != null) {
				conn.createStatement().execute(sqlClima);
				conn.createStatement().execute(sqlVuelos);
				System.out.println("🧱 Tablas creadas correctamente");
			}
		} catch (SQLException e) {
			System.out.println("❌ Error al crear las tablas");
			e.printStackTrace();
		}
	}

	public static Connection connect() {
		try {
			return DriverManager.getConnection(URL);
		} catch (SQLException e) {
			System.out.println("Error al conectar con la base de datos:");
			e.printStackTrace();
			return null;
		}
	}

	public void guardarClima(Clima clima) {
		String sql = "INSERT INTO clima (ciudad, temperatura, humedad, velocidadViento) VALUES (?, ?, ?, ?)";

		try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, clima.getCiudad());
			pstmt.setDouble(2, clima.getTemperatura());
			pstmt.setDouble(3, clima.getHumedad());
			pstmt.setDouble(4, clima.getVelocidadViento());
			pstmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void guardarVuelo(Vuelo vuelo) {
		String sql = "INSERT INTO vuelos (numeroVuelo, aerolinea, aeropuertoSalida, aeropuertoLlegada, estado) VALUES (?, ?, ?, ?, ?)";

		try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, vuelo.getNumeroVuelo());
			pstmt.setString(2, vuelo.getAerolinea());
			pstmt.setString(3, vuelo.getAeropuertoSalida());
			pstmt.setString(4, vuelo.getAeropuertoLlegada());
			pstmt.setString(5, vuelo.getEstado());
			pstmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
