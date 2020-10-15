package com.obj.nc.domain;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.json.JSONObject;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.obj.nc.domain.event.Event;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BaseJSONObject {

	private Map<String, Object> attributes = new HashMap<String, Object>();

	@JsonAnySetter
	public void pubAttributeValue(String key, Object value) {
		attributes.put(key, value);
	}

	public static UUID generateUUID() {
		return UUID.randomUUID();
	}

	public static Event fromJSON(JSONObject jo) {
		return fromJSON(jo.toString());
	}

	public static Event fromJSON(String jsonString) {
		try {
			ObjectMapper objectMapper = new ObjectMapper();

			Event event = objectMapper.readValue(jsonString, Event.class);
			return event;
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	public String toJSONString() {
		try {
			ObjectMapper objectMapper = new ObjectMapper();
			String jsonString = objectMapper.writeValueAsString(this);

			return jsonString;
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

}
