package com.Skysync.main;

public class Config {
	public static final String AVIATIONSTACK_API_KEY = "01d7287072773bf7a13bb12e7d6bf3f9";
	public static final String OPENWEATHER_API_KEY = "e3ab094c405a366e4715800f0ac15040";

	public static final String BROKER_URL = "tcp://localhost:61616";
	public static final String WEATHER_TOPIC = "prediction.Weather";
	public static final String WEATHER_SOURCE = "feederA";
	public static final double UMBRAL_VELOCIDAD_VIENTO = 30.0; // km/h
	public static final double UMBRAL_HUMEDAD = 90.0; // %
}
