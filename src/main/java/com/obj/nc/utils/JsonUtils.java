/*
 *   Copyright (C) 2021 the original author or authors.
 *
 *   This file is part of Notiflow
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Lesser General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public License
 *   along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.obj.nc.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.obj.nc.exceptions.PayloadValidationException;
import org.springframework.integration.support.json.Jackson2JsonObjectMapper;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class JsonUtils {
	
	private static ObjectMapper objectMapperWithSortedProperties;
	private static Jackson2JsonObjectMapper jsonObjectMapperUsingSortedProperties;

	public static <T> T readObjectFromJSONFile(Path filePath, Class<T> beanType) {
		String JSONStr = readFileContent(filePath);	
		T pojo = readObjectFromJSONString(JSONStr,beanType);
		return pojo;
	}

	public static <T> T readObjectFromJSONFileToInstance(Path filePath, T bean) {
		String JSONStr = readFileContent(filePath);	
		T pojo = readObjectFromJSONStringToInstance(JSONStr,bean);
		return pojo;
	}	

	public static void writeObjectToJSONFile(Path filePath, Object bean) {
		String JSONStr = writeObjectToJSONString(bean);
		writeFileContent(filePath, JSONStr);	
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
	
	public static JsonNode readJsonNodeFromClassPathResource(String resourceName) {
		String jsonString = readJsonStringFromClassPathResource(resourceName);
		
		return readJsonNodeFromJSONString(jsonString);
	}
	
	public static <T> T readObjectFromJSONString(String json, Class<T> beanType) {
		
		try {
		    final ObjectMapper objectMapper = getObjectMapperWithSortedProperties();

			T pojo = objectMapper.readValue(json, beanType);
			return pojo;
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}		
	}

	public static <T> T readObjectFromJSONStringToInstance(String json, T bean) {
		
		try {
		    final ObjectMapper objectMapper = getObjectMapperWithSortedProperties();

			T pojo = objectMapper.readerForUpdating(bean).readValue(json);
			return pojo;
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}		
	}
	
	public static JsonNode readJsonNodeFromJSONString(String json) {
		
		try {
		    final ObjectMapper objectMapper = getObjectMapperWithSortedProperties();

			JsonNode jsonNode = objectMapper.readTree(json);
			return jsonNode;
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
		
	}

		
	public static List<JsonNode> readJsonNodeListFromJSONString(String json) {
		
		try {
		    final ObjectMapper objectMapper = getObjectMapperWithSortedProperties();

			JsonNode jsonObject = objectMapper.readTree(json);
			ArrayNode jsonArray = (ArrayNode)jsonObject; 

			List<JsonNode> result = new ArrayList<JsonNode>();     
			jsonArray.iterator().forEachRemaining(a-> result.add(a));

			return result;
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
		
	}
	
	public static JsonNode readJsonNodeFromPojo(Object object) {
		final ObjectMapper objectMapper = getObjectMapperWithSortedProperties();
		return objectMapper.valueToTree(object);
	}
	
	public static <T> T readObjectFromJSON(JsonNode json, Class<T> beanType) {
		try {
		    final ObjectMapper objectMapper = getObjectMapperWithSortedProperties();

			T pojo = objectMapper.treeToValue(json, beanType);
			return pojo;
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
		
	}

	public static <T> T readClassFromObject(Object object, Class<T> clazz) {
	    	final ObjectMapper objectMapper = getObjectMapperWithSortedProperties();
			T pojo = objectMapper.convertValue(object, clazz);
			return pojo;
	}
	
	public static String writeObjectToJSONString(JsonNode json) {
		
		try {
		    final ObjectMapper objectMapper = getObjectMapperWithSortedProperties();
			String jsonString = objectMapper.writeValueAsString(json);

			return jsonString;
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
		
	}
	
	public static String writeObjectToJSONString(Object pojo) {
		
		try {
		    final ObjectMapper objectMapper = getObjectMapperWithSortedProperties();
			String jsonString = objectMapper.writeValueAsString(pojo);

			return jsonString;
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
		
	}
	
	public static JsonNode writeObjectToJSONNode(Object pojo) {
	    final ObjectMapper objectMapper = getObjectMapperWithSortedProperties();
		JsonNode jsonNode = objectMapper.valueToTree(pojo);

		return jsonNode;
	}

	public static String writeObjectToJSONStringPretty(Object pojo) {

		try {
		    final ObjectMapper objectMapper = getObjectMapperWithSortedProperties();
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

	private static void writeFileContent(Path filePath, String content) {
		try {
			byte[] strToBytes = content.getBytes();

			Files.write(filePath, strToBytes);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
   
	
	public static Optional<String> checkValidAndGetError(String jsonString) {
		try {
		       final ObjectMapper mapper = getObjectMapperWithSortedProperties();
		       mapper.readTree(jsonString);
		       return Optional.empty();
		} catch (Exception e) {
		       return Optional.of(e.getMessage());
	    }
	}

	public static ObjectNode createJsonObjectNode() {
		final ObjectMapper mapper = getObjectMapperWithSortedProperties();
		return mapper.createObjectNode();
	}
	
	public static JsonNode checkIfJsonValidAndReturn(String eventJson) {
		Optional<String> jsonProblems = JsonUtils.checkValidAndGetError(eventJson);
    	if (jsonProblems.isPresent()) {
    		throw new PayloadValidationException(jsonProblems.get());
    	}
    	
    	return JsonUtils.readJsonNodeFromJSONString(eventJson);
	}
	
	public static Date convertJsonDateStringToDate(String date) {
		TemporalAccessor ta = DateTimeFormatter.ISO_INSTANT.parse(date);
	    Instant i = Instant.from(ta);
	    Date d = Date.from(i);
	    return d;
	}
	
	//only for tests
	public static void resetObjectMapper( ) {
		objectMapperWithSortedProperties = null;
	}
	//Use objectmapper defined as spring bean
	public static ObjectMapper getObjectMapperWithSortedProperties() {
		if (objectMapperWithSortedProperties == null) {
			objectMapperWithSortedProperties = new ObjectMapper();
			objectMapperWithSortedProperties.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
			objectMapperWithSortedProperties.registerModule(new JavaTimeModule());
			objectMapperWithSortedProperties.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
			objectMapperWithSortedProperties.enable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY);
			objectMapperWithSortedProperties.enable(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS);
		}
		return objectMapperWithSortedProperties;
	}

	public static Jackson2JsonObjectMapper getJsonObjectMapperUsingSortedProperties() {
		if (jsonObjectMapperUsingSortedProperties == null) {
			jsonObjectMapperUsingSortedProperties = new Jackson2JsonObjectMapper(getObjectMapperWithSortedProperties());
		}
		return jsonObjectMapperUsingSortedProperties;
	}

}
