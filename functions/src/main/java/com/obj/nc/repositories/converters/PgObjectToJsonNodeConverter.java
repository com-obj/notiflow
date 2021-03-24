package com.obj.nc.repositories.converters;

import org.postgresql.util.PGobject;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

import com.fasterxml.jackson.databind.JsonNode;
import com.obj.nc.utils.JsonUtils;

import lombok.extern.log4j.Log4j2;

@ReadingConverter
@Log4j2
public class PgObjectToJsonNodeConverter implements Converter<PGobject, JsonNode> {

	@Override
	public JsonNode convert(PGobject json) {
		if (!json.getType().equals("jsonb")) {
			throw new IllegalArgumentException("Not a jsonb column");
		}
			
		return JsonUtils.readJsonNodeFromJSONString(json.getValue());
	}

}
