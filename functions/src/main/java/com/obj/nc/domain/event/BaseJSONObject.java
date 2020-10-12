package com.obj.nc.domain.event;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonAnySetter;

import lombok.Data;

@Data
public class BaseJSONObject {

	private Map<String, String> attributes = new HashMap<String, String>();

	@JsonAnySetter
	public void pubAttributeValue(String key, String value) {
		attributes.put(key, value);
	}

	public static UUID generateUUID() {
		return UUID.randomUUID();
	}



}
