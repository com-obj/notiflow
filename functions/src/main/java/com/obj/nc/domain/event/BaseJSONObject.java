package com.obj.nc.domain.event;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonAnySetter;

import lombok.Data;

@Data
public class BaseJSONObject {

	private Map<String, Object> attributes = new HashMap<String, Object>();

	@JsonAnySetter
	public void pubAttributeValue(String key, Object value) {
		attributes.put(key, value);
	}

	public static UUID generateUUID() {
		return UUID.randomUUID();
	}



}
