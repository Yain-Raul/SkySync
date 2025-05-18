package com.skysync.core.domain.model;


public class DisruptionForecast {
	private String airportCode;
	private String date;
	private double disruptionProbability;
	private Factors factors;

	public DisruptionForecast(String airportCode, String date, double disruptionProbability, Factors factors) {
		this.airportCode = airportCode;
		this.date = date;
		this.disruptionProbability = disruptionProbability;
		this.factors = factors;
	}

	// Getters and setters
	public String getAirportCode() { return airportCode; }
	public void setAirportCode(String airportCode) { this.airportCode = airportCode; }
	public String getDate() { return date; }
	public void setDate(String date) { this.date = date; }
	public double getDisruptionProbability() { return disruptionProbability; }
	public void setDisruptionProbability(double disruptionProbability) { this.disruptionProbability = disruptionProbability; }
	public Factors getFactors() { return factors; }
	public void setFactors(Factors factors) { this.factors = factors; }

	public static class Factors {
		private String windSpeed;
		private String precipitation;
		private String historicalDelays;

		public Factors(String windSpeed, String precipitation, String historicalDelays) {
			this.windSpeed = windSpeed;
			this.precipitation = precipitation;
			this.historicalDelays = historicalDelays;
		}

		// Getters and setters
		public String getWindSpeed() { return windSpeed; }
		public void setWindSpeed(String windSpeed) { this.windSpeed = windSpeed; }
		public String getPrecipitation() { return precipitation; }
		public void setPrecipitation(String precipitation) { this.precipitation = precipitation; }
		public String getHistoricalDelays() { return historicalDelays; }
		public void setHistoricalDelays(String historicalDelays) { this.historicalDelays = historicalDelays; }
	}
}