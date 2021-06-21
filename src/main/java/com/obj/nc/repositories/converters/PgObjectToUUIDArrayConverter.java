package com.obj.nc.repositories.converters;

import java.util.UUID;

import org.postgresql.util.PGobject;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;

import lombok.extern.log4j.Log4j2;

@WritingConverter
@Log4j2
public class PgObjectToUUIDArrayConverter implements Converter<PGobject, UUID[]> {

	@Override
	public UUID[] convert(PGobject json) {
		if (!json.getType().equals("_uuid")) {
			throw new IllegalArgumentException("Not a uuid[] column");
		}
			
		System.out.println(json.getValue());
		UUID[] result = new UUID[0];
		return result;
	}

}
