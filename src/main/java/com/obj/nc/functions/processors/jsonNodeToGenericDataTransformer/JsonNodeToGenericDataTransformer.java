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

package com.obj.nc.functions.processors.jsonNodeToGenericDataTransformer;

import org.springframework.stereotype.Component;

import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.obj.nc.domain.dataObject.GenericData;
import com.obj.nc.domain.dataObject.GenericDataJson;
import com.obj.nc.domain.dataObject.GenericDataPojo;
import com.obj.nc.functions.processors.ProcessorFunctionAdapter;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class JsonNodeToGenericDataTransformer extends ProcessorFunctionAdapter<List<JsonNode>, GenericData<?>> {

    private String resultSetPojoFccn;

    @Override
	protected GenericData<?> execute(List<JsonNode> jsonNodes) {

        GenericDataJson jsonData = new GenericDataJson(jsonNodes);
        if (resultSetPojoFccn==null) {
            return jsonData;
        }

        try {
            Class<?> pojoClass = Class.forName(resultSetPojoFccn);

            List<?> pojos = jsonData.getPayloadsAsPojo(pojoClass);

            GenericDataPojo<?> pojoData = new GenericDataPojo(pojos);
            return pojoData;    
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
	}



}
