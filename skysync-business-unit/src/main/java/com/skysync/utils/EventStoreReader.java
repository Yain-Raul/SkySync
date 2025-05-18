package com.skysync.utils;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class EventStoreReader {
	private static final Gson gson = new Gson();

	public static List<JsonObject> readEvents(String basePath, LocalDate startDate, LocalDate endDate) {
		List<JsonObject> events = new ArrayList<>();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");

		LocalDate currentDate = startDate;
		while (!currentDate.isAfter(endDate)) {
			String filePath = basePath + "/" + currentDate.format(formatter) + ".events";
			File file = new File(filePath);
			if (file.exists()) {
				try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
					String line;
					while ((line = reader.readLine()) != null) {
						events.add(gson.fromJson(line, JsonObject.class));
					}
				} catch (Exception e) {
					System.err.println("Error reading " + filePath + ": " + e.getMessage());
				}
			}
			currentDate = currentDate.plusDays(1);
		}

		return events;
	}
}