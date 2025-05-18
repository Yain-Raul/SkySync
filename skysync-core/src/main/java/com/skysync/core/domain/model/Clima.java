package com.skysync.core.domain.model;


import java.time.Instant;

public class Clima {

	private String ciudad;
	private String airportCode; // Added to store IATA code
	private double temperatura;
	private double humedad;
	private double velocidadViento;
	private String condicion;

	private Instant timestamp;
	public Clima(String ciudad, String airportCode, double temperatura, double humedad, double velocidadViento, String condicion) {
		this.ciudad = ciudad;
		this.airportCode = airportCode;
		this.temperatura = temperatura;
		this.humedad = humedad;
		this.velocidadViento = velocidadViento;
		this.condicion = condicion;
	}

	public Clima(String ciudad, String airportCode, double temperatura, double humedad, double velocidadViento) {
		this(ciudad, airportCode, temperatura, humedad, velocidadViento, "Desconocido");
	}

	// Constructor vacío necesario para carga de datos
	public Clima() {
	}

	// Getters
	public Instant getTimestamp() {
		return timestamp;
	}

	public String getCiudad() {
		return ciudad;
	}

	public String getAirportCode() {
		return airportCode;
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

	public void setTimestamp(Instant timestamp) {
		this.timestamp = timestamp;
	}

		public void setAirportCode(String airportCode) {
		this.airportCode = airportCode;
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
		return String.format("Clima{ciudad='%s', airportCode='%s', temperatura=%.1f, humedad=%.1f, viento=%.1f, condición='%s'}",
				ciudad, airportCode, temperatura, humedad, velocidadViento, condicion);
	}
} 	