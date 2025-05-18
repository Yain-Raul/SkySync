package com.skysync.core.aplication.ports.out;

import com.skysync.core.domain.model.DisruptionForecast;

public interface DisruptionForecastPort {
	DisruptionForecast forecastDisruption(String airportCode, String date);
}