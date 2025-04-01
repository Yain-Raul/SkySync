package com.Skysync.models;

public class Clima {
	private String ciudad;
	private double temperatura;
	private double humedad;
	private double velocidadViento;

	public Clima(String ciudad, double temperatura, double humedad, double velocidadViento) {
		this.ciudad = ciudad;
		this.temperatura = temperatura;
		this.humedad = humedad;
		this.velocidadViento = velocidadViento;
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

	@Override
	public String toString() {
		return "Clima{" +
				"ciudad='" + ciudad + '\'' +
				", temperatura=" + temperatura +
				", humedad=" + humedad +
				", velocidadViento=" + velocidadViento +
				'}';
	}
}
