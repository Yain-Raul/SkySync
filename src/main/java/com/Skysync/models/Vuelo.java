package com.Skysync.models;

public class Vuelo {
	private String numeroVuelo;
	private String aerolinea;
	private String aeropuertoSalida;
	private String aeropuertoSalidaIATA;  // 👈 NUEVO
	private String aeropuertoLlegada;
	private String estado;

	public Vuelo(String numeroVuelo, String aerolinea, String aeropuertoSalida, String aeropuertoSalidaIATA,
				 String aeropuertoLlegada, String estado) {
		this.numeroVuelo = numeroVuelo;
		this.aerolinea = aerolinea;
		this.aeropuertoSalida = aeropuertoSalida;
		this.aeropuertoSalidaIATA = aeropuertoSalidaIATA;
		this.aeropuertoLlegada = aeropuertoLlegada;
		this.estado = estado;
	}

	// Getters
	public String getNumeroVuelo() {
		return numeroVuelo;
	}

	public String getAerolinea() {
		return aerolinea;
	}

	public String getAeropuertoSalida() {
		return aeropuertoSalida;
	}

	public String getAeropuertoSalidaIATA() {
		return aeropuertoSalidaIATA;
	}

	public String getAeropuertoLlegada() {
		return aeropuertoLlegada;
	}

	public String getEstado() {
		return estado;
	}

	// Setters
	public void setAeropuertoSalidaIATA(String aeropuertoSalidaIATA) {
		this.aeropuertoSalidaIATA = aeropuertoSalidaIATA;
	}

	@Override
	public String toString() {
		return "Vuelo{" +
				"numeroVuelo='" + numeroVuelo + '\'' +
				", aerolinea='" + aerolinea + '\'' +
				", aeropuertoSalida='" + aeropuertoSalida + '\'' +
				", aeropuertoLlegada='" + aeropuertoLlegada + '\'' +
				", estado='" + estado + '\'' +
				'}';
	}
}

