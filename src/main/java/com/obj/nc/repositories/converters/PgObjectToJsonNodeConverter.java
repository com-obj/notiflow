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

import com.fasterxml.jackson.databind.JsonNode;
import com.obj.nc.utils.JsonUtils;
import org.postgresql.util.PGobject;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

@ReadingConverter
public class PgObjectToJsonNodeConverter implements Converter<PGobject, JsonNode> {

	@Override
	public JsonNode convert(PGobject json) {
		if (!json.getType().equals("jsonb")) {
			throw new IllegalArgumentException("Not a jsonb column");
		}
			
		return JsonUtils.readJsonNodeFromJSONString(json.getValue());
	}

}
