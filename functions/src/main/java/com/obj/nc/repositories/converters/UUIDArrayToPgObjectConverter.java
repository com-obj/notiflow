package com.obj.nc.repositories.converters;

import java.sql.SQLException;
import java.util.StringJoiner;
import java.util.UUID;

import org.postgresql.util.PGobject;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;

import lombok.extern.log4j.Log4j2;

@WritingConverter
@Log4j2
public class UUIDArrayToPgObjectConverter implements Converter<UUID[], PGobject> {

	@Override
	public PGobject convert(UUID[] uuids) {
		try {
			PGobject out = new PGobject();
			out.setType("uuid[]");
			StringJoiner joiner = new StringJoiner(",");
			for (UUID uuid: uuids) {
				joiner.add("\"" + uuid.toString() + "\"");
			}
			
			String uuidsAsString = "{"+joiner.toString()+"}";
			out.setValue(uuidsAsString);
			return out;
		} catch (SQLException e) {
			log.error("StringToJsonConverter failed to convert " + uuids + " to PGObject with json type");
			throw new IllegalArgumentException(e);
		}
	}

}
