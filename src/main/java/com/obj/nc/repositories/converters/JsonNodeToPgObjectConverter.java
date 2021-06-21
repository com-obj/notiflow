package com.obj.nc.repositories.converters;

import java.sql.SQLException;

import org.postgresql.util.PGobject;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;

import com.fasterxml.jackson.databind.JsonNode;
import com.obj.nc.utils.JsonUtils;

import lombok.extern.log4j.Log4j2;

@WritingConverter
@Log4j2
public class JsonNodeToPgObjectConverter implements Converter<JsonNode, PGobject> {

	@Override
	public PGobject convert(JsonNode json) {
		try {
			PGobject out = new PGobject();
			out.setType("jsonb");
			out.setValue(JsonUtils.writeObjectToJSONString(json));
			return out;
		} catch (SQLException e) {
			log.error("StringToJsonConverter failed to convert " + json + " to PGObject with json type");
			throw new IllegalArgumentException(e);
		}
	}

}
