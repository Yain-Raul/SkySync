package com.Skysync.models;

public class Clima {

	private String ciudad;
	private double temperatura;
	private double humedad;
	private double velocidadViento;
	private String condicion; // NUEVO: "Clear", "Rain", etc.

	public Clima(String ciudad, double temperatura, double humedad, double velocidadViento, String condicion) {
		this.ciudad = ciudad;
		this.temperatura = temperatura;
		this.humedad = humedad;
		this.velocidadViento = velocidadViento;
		this.condicion = condicion;
	}

	// Si estás usando otro constructor, mantenlo también
	public Clima(String ciudad, double temperatura, double humedad, double velocidadViento) {
		this(ciudad, temperatura, humedad, velocidadViento, "Desconocido");
	}

	public String getCiudad() {
		return ciudad;
	}

	public double getTemperatura() {
		return temperatura;
	}

	public double getHumedad() {
		return humedad;
	}

	public double getVelocidadViento() {
		return velocidadViento;
	}

	public String getCondicion() {
		return condicion;
	}

	@Override
	public String toString() {
		return String.format("Clima{ciudad='%s', temperatura=%.1f, humedad=%.1f, viento=%.1f, condición='%s'}",
				ciudad, temperatura, humedad, velocidadViento, condicion);
	}
}
