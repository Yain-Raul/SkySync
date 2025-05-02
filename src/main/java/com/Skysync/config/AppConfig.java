package com.Skysync.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class AppConfig {
	private static final Properties props = new Properties();


	static {
		try (InputStream input = AppConfig.class.getClassLoader().getResourceAsStream("application.properties")) {
			if (input != null) {
				props.load(input);
			} else {
				System.out.println("❌ Config file not found.");
			}
			props.load(input);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static String get(String key) {
		String value = props.getProperty(key);
		if (value == null) {
			System.err.println("⚠️ La clave '" + key + "' no existe en application.properties.");
			throw new IllegalArgumentException("❌ Clave no encontrada: " + key);
		}
		return value.trim();
	}


	public static double getDouble(String key) {
		return Double.parseDouble(props.getProperty(key));
	}
}
