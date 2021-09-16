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
