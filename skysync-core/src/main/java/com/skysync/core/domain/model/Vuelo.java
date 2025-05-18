package com.skysync.core.domain.model;

public class Vuelo {
	private String numeroVuelo;
	private String aerolinea;
	private String aeropuertoSalida;
	private String aeropuertoSalidaIATA;
	private String aeropuertoLlegada;
	private String estado;
	private String fecha; // Added field for date (e.g., "2025-05-18")
	private String razonRetraso; // Optional field for delay reason

	// Constructor completo
	public Vuelo(String numeroVuelo, String aerolinea, String aeropuertoSalida, String aeropuertoSalidaIATA,
				 String aeropuertoLlegada, String estado, String fecha, String razonRetraso) {
		this.numeroVuelo = numeroVuelo;
		this.aerolinea = aerolinea;
		this.aeropuertoSalida = aeropuertoSalida;
		this.aeropuertoSalidaIATA = aeropuertoSalidaIATA;
		this.aeropuertoLlegada = aeropuertoLlegada;
		this.estado = estado;
		this.fecha = fecha;
		this.razonRetraso = razonRetraso;
	}

	// Constructor vacío (útil para carga desde base de datos)
	public Vuelo() {
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

	public String getFecha() {
		return fecha;
	}

	public String getRazonRetraso() {
		return razonRetraso;
	}

	// Setters
	public void setNumeroVuelo(String numeroVuelo) {
		this.numeroVuelo = numeroVuelo;
	}

	public void setAerolinea(String aerolinea) {
		this.aerolinea = aerolinea;
	}

	public void setAeropuertoSalida(String aeropuertoSalida) {
		this.aeropuertoSalida = aeropuertoSalida;
	}

	public void setAeropuertoSalidaIATA(String aeropuertoSalidaIATA) {
		this.aeropuertoSalidaIATA = aeropuertoSalidaIATA;
	}

	public void setAeropuertoLlegada(String aeropuertoLlegada) {
		this.aeropuertoLlegada = aeropuertoLlegada;
	}

	public void setEstado(String estado) {
		this.estado = estado;
	}

	public void setFecha(String fecha) {
		this.fecha = fecha;
	}

	public void setRazonRetraso(String razonRetraso) {
		this.razonRetraso = razonRetraso;
	}

	@Override
	public String toString() {
		return "Vuelo{" +
				"numeroVuelo='" + numeroVuelo + '\'' +
				", aerolinea='" + aerolinea + '\'' +
				", aeropuertoSalida='" + aeropuertoSalida + '\'' +
				", aeropuertoSalidaIATA='" + aeropuertoSalidaIATA + '\'' +
				", aeropuertoLlegada='" + aeropuertoLlegada + '\'' +
				", estado='" + estado + '\'' +
				", fecha='" + fecha + '\'' +
				", razonRetraso='" + razonRetraso + '\'' +
				'}';
	}
}