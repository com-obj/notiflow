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

import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.JsonNode;
import com.obj.nc.domain.dataObject.GenericData;
import com.obj.nc.functions.processors.ProcessorFunctionAdapter;
import com.obj.nc.functions.processors.spelFilter.SpELFilterPojo;
import com.obj.nc.functions.processors.spelFilter.SpELFilterJson;
import com.obj.nc.utils.JsonUtils;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class JsonNodeFilterAndTransformer extends ProcessorFunctionAdapter<List<JsonNode>, List<?>> {

    private String resultSetPojoFCCN;
    private String SpELFilterExpression;
    
    @Override
	protected List<?> execute(List<JsonNode> jsonNodes) {

        if (resultSetPojoFCCN==null) {
            return filterJsonIfApplicable(jsonNodes);
        }

        try {
            Class<?> pojoClass = Class.forName(resultSetPojoFCCN);

            List<?> pojos = 
                jsonNodes.stream()
                .map(node -> JsonUtils.readObjectFromJSON(node, pojoClass))
                .collect(Collectors.toList());

            return filterPojosIfApplicable(pojos);    
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
	}

    private List<?> filterPojosIfApplicable(List pojos) {
        if (SpELFilterExpression==null) {
            return pojos;
        }

        SpELFilterPojo<?> filter = new SpELFilterPojo<>(SpELFilterExpression);
        return filter.apply(pojos);   
    }

    private List<JsonNode> filterJsonIfApplicable(List<JsonNode> jsonNodes) {
        if (SpELFilterExpression==null) {
            return jsonNodes;
        }

        SpELFilterJson filter = new SpELFilterJson(SpELFilterExpression);
        return filter.apply(jsonNodes);        
    }



}
