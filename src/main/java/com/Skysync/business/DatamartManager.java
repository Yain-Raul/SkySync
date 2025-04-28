package com.Skysync.business;

import com.Skysync.models.Clima;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.Skysync.models.Vuelo;

public class DatamartManager {

	private static final String DB_URL = "jdbc:sqlite:datamart.db";

	public DatamartManager() {
		crearTablaClima();
		crearTablaVuelos();
	}

	private Connection connect() {
		try {
			return DriverManager.getConnection(DB_URL);
		} catch (SQLException e) {
			System.out.println("❌ Error conectando a Datamart:");
			e.printStackTrace();
			return null;
		}
	}

	private void crearTablaClima() {
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

		try (Connection conn = connect()) {
			if (conn != null) {
				conn.createStatement().execute(sql);
				System.out.println("🧱 Tabla clima_datamart creada (o ya existe)");
			}
		} catch (SQLException e) {
			System.out.println("❌ Error creando tabla clima_datamart:");
			e.printStackTrace();
		}
	}

	public void insertarClima(Clima clima) {
		String sql = """
            INSERT INTO clima_datamart (ciudad, temperatura, humedad, velocidadViento, condicion, timestamp)
            VALUES (?, ?, ?, ?, ?, datetime('now'));
        """;

		try (Connection conn = connect();
			 PreparedStatement pstmt = conn.prepareStatement(sql)) {

			pstmt.setString(1, clima.getCiudad());
			pstmt.setDouble(2, clima.getTemperatura());
			pstmt.setDouble(3, clima.getHumedad());
			pstmt.setDouble(4, clima.getVelocidadViento());
			pstmt.setString(5, clima.getCondicion());

			pstmt.executeUpdate();
			System.out.println("📥 Insertado clima en datamart: " + clima.getCiudad());

		} catch (SQLException e) {
			System.out.println("❌ Error insertando clima:");
			e.printStackTrace();
		}
	}

	public void mostrarResumenClimaPorCiudad() {
		String sql = """
            SELECT ciudad,
                   AVG(temperatura) AS temp_media,
                   AVG(humedad) AS humedad_media,
                   AVG(velocidadViento) AS viento_medio
            FROM clima_datamart
            GROUP BY ciudad
            ORDER BY ciudad;
        """;

		try (Connection conn = connect();
			 PreparedStatement stmt = conn.prepareStatement(sql);
			 ResultSet rs = stmt.executeQuery()) {

			System.out.println("\n📊 Resumen de clima promedio por ciudad:");
			while (rs.next()) {
				String ciudad = rs.getString("ciudad");
				double tempMedia = rs.getDouble("temp_media");
				double humedadMedia = rs.getDouble("humedad_media");
				double vientoMedio = rs.getDouble("viento_medio");

				System.out.printf("- %s → 🌡️ %.1f°C, 💧 %.0f%%, 💨 %.1f km/h%n",
						ciudad, tempMedia, humedadMedia, vientoMedio);
			}

		} catch (SQLException e) {
			System.out.println("❌ Error consultando clima promedio:");
			e.printStackTrace();
		}
	}

	public void detectarCondicionesExtremas() {
		String sql = """
            SELECT ciudad, temperatura, humedad, velocidadViento, condicion, timestamp
            FROM clima_datamart
            WHERE velocidadViento > 30
               OR humedad > 90;
        """;

		try (Connection conn = connect();
			 PreparedStatement stmt = conn.prepareStatement(sql);
			 ResultSet rs = stmt.executeQuery()) {

			System.out.println("\n🚨 Condiciones extremas detectadas:");

			boolean hayAlertas = false;

			while (rs.next()) {
				hayAlertas = true;
				String ciudad = rs.getString("ciudad");
				double temp = rs.getDouble("temperatura");
				double humedad = rs.getDouble("humedad");
				double viento = rs.getDouble("velocidadViento");
				String condicion = rs.getString("condicion");
				String timestamp = rs.getString("timestamp");

				System.out.printf("- %s [%s] → 🌡️ %.1f°C, 💧 %.0f%%, 💨 %.1f km/h, ☁️ %s%n",
						ciudad, timestamp, temp, humedad, viento, condicion);
			}

			if (!hayAlertas) {
				System.out.println("✅ No se detectaron condiciones extremas en los registros actuales.");
			}

		} catch (SQLException e) {
			System.out.println("❌ Error detectando condiciones extremas:");
			e.printStackTrace();
		}
	}

	private void crearTablaVuelos() {
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

		try (Connection conn = connect()) {
			if (conn != null) {
				conn.createStatement().execute(sql);
				System.out.println("🧱 Tabla vuelos_datamart creada (o ya existe)");
			}
		} catch (SQLException e) {
			System.out.println("❌ Error creando tabla vuelos_datamart:");
			e.printStackTrace();
		}
	}

	public void insertarVuelo(Vuelo vuelo) {
		String sql = """
        INSERT INTO vuelos_datamart (numeroVuelo, aerolinea, aeropuertoSalida, aeropuertoLlegada, estado, timestamp)
        VALUES (?, ?, ?, ?, ?, datetime('now'));
    """;

		try (Connection conn = connect();
			 PreparedStatement pstmt = conn.prepareStatement(sql)) {

			pstmt.setString(1, vuelo.getNumeroVuelo());
			pstmt.setString(2, vuelo.getAerolinea());
			pstmt.setString(3, vuelo.getAeropuertoSalida());
			pstmt.setString(4, vuelo.getAeropuertoLlegada());
			pstmt.setString(5, vuelo.getEstado());

			pstmt.executeUpdate();
			System.out.println("📥 Insertado vuelo en datamart: " + vuelo.getNumeroVuelo());

		} catch (SQLException e) {
			System.out.println("❌ Error insertando vuelo:");
			e.printStackTrace();
		}
	}

	public void mostrarEstadoVuelos() {
		String sqlRetrasados = """
        SELECT COUNT(*) AS total_delays
        FROM vuelos_datamart
        WHERE estado LIKE '%delay%';
    """;

		String sqlCancelados = """
        SELECT COUNT(*) AS total_canceled
        FROM vuelos_datamart
        WHERE estado LIKE '%cancel%';
    """;

		try (Connection conn = connect()) {
			if (conn != null) {
				PreparedStatement stmtDelay = conn.prepareStatement(sqlRetrasados);
				PreparedStatement stmtCancel = conn.prepareStatement(sqlCancelados);

				ResultSet rsDelay = stmtDelay.executeQuery();
				ResultSet rsCancel = stmtCancel.executeQuery();

				if (rsDelay.next() && rsCancel.next()) {
					int totalDelays = rsDelay.getInt("total_delays");
					int totalCanceled = rsCancel.getInt("total_canceled");

					System.out.println("\n📊 Estado actual de los vuelos:");
					System.out.println("- ✈️ Vuelos retrasados: " + totalDelays);
					System.out.println("- 🛑 Vuelos cancelados: " + totalCanceled);
				}
			}
		} catch (SQLException e) {
			System.out.println("❌ Error mostrando estado de vuelos:");
			e.printStackTrace();
		}
	}

	public int contarClimasExtremos() {
		String sql = """
        SELECT COUNT(*) AS total
        FROM clima_datamart
        WHERE velocidadViento > 30
           OR humedad > 90;
    """;

		try (Connection conn = connect();
			 PreparedStatement stmt = conn.prepareStatement(sql);
			 ResultSet rs = stmt.executeQuery()) {

			if (rs.next()) {
				return rs.getInt("total");
			}

		} catch (SQLException e) {
			System.out.println("❌ Error contando climas extremos:");
			e.printStackTrace();
		}
		return 0;
	}

	public int contarVuelosRetrasadosActuales() {
		String sql = """
        SELECT COUNT(*) AS total
        FROM vuelos_datamart
        WHERE estado LIKE '%delay%';
    """;

		try (Connection conn = connect();
			 PreparedStatement stmt = conn.prepareStatement(sql);
			 ResultSet rs = stmt.executeQuery()) {

			if (rs.next()) {
				return rs.getInt("total");
			}

		} catch (SQLException e) {
			System.out.println("❌ Error contando vuelos retrasados:");
			e.printStackTrace();
		}
		return 0;
	}



}
