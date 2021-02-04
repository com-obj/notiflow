package com.obj.nc.utils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.obj.nc.domain.event.Event;

public class JsonUtils {

	public static <T> T readObjectFromJSONFile(Path filePath, Class<T> beanType) {
		String JSONStr = readFileContent(filePath);	
		T pojo = readObjectFromJSONString(JSONStr,beanType);
		return pojo;
	}
	
	public static <T> T readObjectFromClassPathResource(String resourceName, Class<T> beanType) {
		ClassLoader classLoader = JsonUtils.class.getClassLoader();
		URL fileURL = classLoader.getResource(resourceName);
		if (fileURL == null) {
			throw new IllegalArgumentException("File " +resourceName + " not found on classpath");
		}
		File file = new File(fileURL.getFile());

		return readObjectFromJSONFile(file.toPath(),beanType);
	}
	
	public static <T> T readObjectFromJSONString(String json, Class<T> beanType) {
		
		try {
			ObjectMapper objectMapper = new ObjectMapper();

			T pojo = objectMapper.readValue(json, beanType);
			return pojo;
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
		
	}
	
	public static String writeObjectToJSONString(Object pojo) {
		
		try {
			ObjectMapper objectMapper = new ObjectMapper();
			String jsonString = objectMapper.writeValueAsString(pojo);

			return jsonString;
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
		
	}

	public static String writeObjectToJSONStringPretty(Object pojo) {

		try {
			ObjectMapper objectMapper = new ObjectMapper();
			String jsonString = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(pojo);

			return jsonString;
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}

	}
	
	private static String readFileContent(Path filePath)
    {
		try {
			StringBuilder contentBuilder = new StringBuilder();

			try (Stream<String> stream = Files.lines(filePath, StandardCharsets.UTF_8)) {
				stream.forEach(s -> contentBuilder.append(s).append("\n"));
			}

			return contentBuilder.toString();
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
    }
}
