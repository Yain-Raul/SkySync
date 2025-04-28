package com.Skysync.database;

import com.Skysync.models.Clima;
import com.Skysync.models.Vuelo;

import java.sql.*;

public class DatabaseManager {
	private static final String URL = "jdbc:sqlite:datamart.db";

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
		String sqlCheck = """
	    SELECT COUNT(*) FROM clima
	    WHERE ciudad = ? AND DATE(fecha) = DATE('now')
	""";

		String sqlInsert = """
	    INSERT INTO clima (ciudad, temperatura, humedad, velocidadViento)
	    VALUES (?, ?, ?, ?)
	""";

		try (Connection conn = connect();
			 PreparedStatement checkStmt = conn.prepareStatement(sqlCheck);
			 PreparedStatement insertStmt = conn.prepareStatement(sqlInsert)) {

			checkStmt.setString(1, clima.getCiudad());
			ResultSet rs = checkStmt.executeQuery();
			rs.next();
			int count = rs.getInt(1);

			if (count > 0) {
				System.out.println("⚠️ Clima ya registrado hoy en: " + clima.getCiudad());
				return;
			}

			insertStmt.setString(1, clima.getCiudad());
			insertStmt.setDouble(2, clima.getTemperatura());
			insertStmt.setDouble(3, clima.getHumedad());
			insertStmt.setDouble(4, clima.getVelocidadViento());
			insertStmt.executeUpdate();

			System.out.println("✅ Clima insertado para: " + clima.getCiudad());

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void guardarVuelo(Vuelo vuelo) {
		String sqlCheck = """
	    SELECT COUNT(*) FROM vuelos
	    WHERE numeroVuelo = ? AND aerolinea = ? AND aeropuertoSalida = ?
	          AND aeropuertoLlegada = ? AND estado = ? AND DATE(fecha) = DATE('now')
	""";

		String sqlInsert = """
	    INSERT INTO vuelos (numeroVuelo, aerolinea, aeropuertoSalida, aeropuertoLlegada, estado)
	    VALUES (?, ?, ?, ?, ?)
	""";

		try (Connection conn = connect();
			 PreparedStatement checkStmt = conn.prepareStatement(sqlCheck);
			 PreparedStatement insertStmt = conn.prepareStatement(sqlInsert)) {

			checkStmt.setString(1, vuelo.getNumeroVuelo());
			checkStmt.setString(2, vuelo.getAerolinea());
			checkStmt.setString(3, vuelo.getAeropuertoSalida());
			checkStmt.setString(4, vuelo.getAeropuertoLlegada());
			checkStmt.setString(5, vuelo.getEstado());

			ResultSet rs = checkStmt.executeQuery();
			rs.next();
			int count = rs.getInt(1);

			if (count > 0) {
				System.out.println("⚠️ Vuelo ya registrado hoy: " + vuelo.getNumeroVuelo());
				return;
			}

			insertStmt.setString(1, vuelo.getNumeroVuelo());
			insertStmt.setString(2, vuelo.getAerolinea());
			insertStmt.setString(3, vuelo.getAeropuertoSalida());
			insertStmt.setString(4, vuelo.getAeropuertoLlegada());
			insertStmt.setString(5, vuelo.getEstado());
			insertStmt.executeUpdate();

			System.out.println("✅ Vuelo insertado: " + vuelo.getNumeroVuelo());

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
