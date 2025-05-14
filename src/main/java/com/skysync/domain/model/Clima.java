package com.skysync.domain.model;

public class Clima {

	private String ciudad;
	private double temperatura;
	private double humedad;
	private double velocidadViento;
	private String condicion;

	public Clima(String ciudad, double temperatura, double humedad, double velocidadViento, String condicion) {
		this.ciudad = ciudad;
		this.temperatura = temperatura;
		this.humedad = humedad;
		this.velocidadViento = velocidadViento;
		this.condicion = condicion;
	}

	public Clima(String ciudad, double temperatura, double humedad, double velocidadViento) {
		this(ciudad, temperatura, humedad, velocidadViento, "Desconocido");
	}

	// 🚨 Constructor vacío necesario para carga de datos
	public Clima() {
	}

	// Getters
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

	// Setters
	public void setCiudad(String ciudad) {
		this.ciudad = ciudad;
	}

	public void setTemperatura(double temperatura) {
		this.temperatura = temperatura;
	}

	public void setHumedad(double humedad) {
		this.humedad = humedad;
	}

	public void setVelocidadViento(double velocidadViento) {
		this.velocidadViento = velocidadViento;
	}

	public void setCondicion(String condicion) {
		this.condicion = condicion;
	}

	@Override
	public String toString() {
		return String.format("Clima{ciudad='%s', temperatura=%.1f, humedad=%.1f, viento=%.1f, condición='%s'}",
				ciudad, temperatura, humedad, velocidadViento, condicion);
	}
}
