package com.obj.nc.utils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import org.json.JSONException;
import org.json.JSONObject;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.obj.nc.domain.event.Event;
import com.obj.nc.exceptions.PayloadValidationException;

public class JsonUtils {

	public static <T> T readObjectFromJSONFile(Path filePath, Class<T> beanType) {
		String JSONStr = readFileContent(filePath);	
		T pojo = readObjectFromJSONString(JSONStr,beanType);
		return pojo;
	}
	
	public static <T> T readObjectFromClassPathResource(String resourceName, Class<T> beanType) {
		String JSONStr =  readJsonStringFromClassPathResource(resourceName);

		T pojo = readObjectFromJSONString(JSONStr, beanType);
		return pojo;
	}
	
	public static String readJsonStringFromClassPathResource(String resourceName) {
		ClassLoader classLoader = JsonUtils.class.getClassLoader();
		URL fileURL = classLoader.getResource(resourceName);
		if (fileURL == null) {
			throw new IllegalArgumentException("File " +resourceName + " not found on classpath");
		}
		File file = new File(fileURL.getFile());

		return readFileContent(file.toPath());
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
	
	public static JsonNode readObjectFromJSONString(String json) {
		
		try {
			ObjectMapper objectMapper = new ObjectMapper();

			JsonNode jsonNode = objectMapper.readTree(json);
			return jsonNode;
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
		
	}
	
	public static <T> T readObjectFromJSON(JsonNode json, Class<T> beanType) {
		try {
			ObjectMapper objectMapper = new ObjectMapper();

			T pojo = objectMapper.treeToValue(json, beanType);
			return pojo;
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
		
	}

	public static <T> T readClassFromObject(Object object, Class<T> clazz) {
			ObjectMapper objectMapper = new ObjectMapper();
			T pojo = objectMapper.convertValue(object, clazz);
			return pojo;
	}
	
	public static String writeObjectToJSONString(JsonNode json) {
		
		try {
			ObjectMapper objectMapper = new ObjectMapper();
			String jsonString = objectMapper.writeValueAsString(json);

			return jsonString;
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
	
	public static JsonNode writeObjectToJSONNode(Object pojo) {
		ObjectMapper objectMapper = new ObjectMapper();
		JsonNode jsonNode = objectMapper.valueToTree(pojo);

		return jsonNode;
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
	
	public static Optional<String> checkValidAndGetError(String jsonString) {
		try {
		       final ObjectMapper mapper = new ObjectMapper();
		       mapper.readTree(jsonString);
		       return Optional.empty();
		} catch (Exception e) {
		       return Optional.of(e.getMessage());
	    }
	}
	
	public static JsonNode checkIfJsonValidAndReturn(String eventJson) {
		Optional<String> jsonProblems = JsonUtils.checkValidAndGetError(eventJson);
    	if (jsonProblems.isPresent()) {
    		throw new PayloadValidationException(jsonProblems.get());
    	}
    	
    	return JsonUtils.readObjectFromJSONString(eventJson);
	}
}
