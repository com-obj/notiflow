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

package com.obj.nc.domain;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.databind.JsonNode;
import com.obj.nc.utils.JsonUtils;
import lombok.Data;
import org.springframework.data.annotation.AccessType;
import org.springframework.data.annotation.Transient;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@AccessType(AccessType.Type.PROPERTY)
public class BaseDynamicAttributesBean {

	@Transient
	private Map<String, Object> attributes = new HashMap<String, Object>();

	@JsonAnySetter
	public void setAttributeValue(String key, Object value) {
		attributes.put(key, value);
	}
	
	public boolean hasAttribute(String key) {
		return attributes.containsKey(key);
	}

	public static UUID generateUUID() {
		return UUID.randomUUID();
	}

	public String toJSONString() {
		return JsonUtils.writeObjectToJSONString(this);
	}
	
	public JsonNode toJSONNode() {
		return JsonUtils.writeObjectToJSONNode(this);
	}

	public boolean containsAttributes(List<String> attributes) {
		return attributes.stream().allMatch(attr -> this.attributes.get(attr) != null);
	}

	public boolean containsAttribute(String attribute) {
		return containsAttributes(Collections.singletonList(attribute));
	}

	public boolean containsNestedAttributes(String... attributePaths) {

		Map<String, Object> fieldValue = getAttributeValueAs(attributePaths[0], Map.class);
		if (fieldValue == null) {
			return false;
		}

		for (int i = 1; i < attributePaths.length; i++) {
			fieldValue = JsonUtils.readClassFromObject(fieldValue.get(attributePaths[i]), Map.class);
			if (fieldValue == null) {
				return false;
			}
		}

		return true;
	}

	public <T> T getAttributeValueAs(String attributeName, Class<T> clazz) {
		return JsonUtils.readClassFromObject(attributes.get(attributeName), clazz);
	}
	
	public Object getAttributeValue(String attributeName) {
		return attributes.get(attributeName);
	}
	
}
