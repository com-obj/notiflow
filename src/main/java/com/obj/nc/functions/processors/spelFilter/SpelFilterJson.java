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
package com.obj.nc.functions.processors.spelFilter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.obj.nc.aspects.DocumentProcessingInfo;
import com.obj.nc.exceptions.ProcessingException;
import com.obj.nc.functions.processors.ProcessorFunctionAdapter;

import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.integration.json.JsonPropertyAccessor;

import lombok.extern.log4j.Log4j2;

@Log4j2
@DocumentProcessingInfo("SpELFilterJson")
public class SpELFilterJson  extends ProcessorFunctionAdapter<List<JsonNode>, List<JsonNode>> {

    private final ExpressionParser parser;
    private final Expression expression;

    public SpELFilterJson(String spelExpression) {
        parser = new SpelExpressionParser();
        expression = parser.parseExpression(spelExpression);
    }

    @Override
    protected List<JsonNode> execute(List<JsonNode> payloads) {
        List<JsonNode> filteredList = new ArrayList<>();
        for (JsonNode payload: payloads) {
            StandardEvaluationContext context = new StandardEvaluationContext();
            context.setPropertyAccessors(Arrays.asList(new JsonPropertyAccessor()));
            context.setRootObject(payload);

            Object value = expression.getValue(context);

            if (!(value instanceof Boolean)) {
                throw new ProcessingException("Filter SpEL expects expression that returns boolean. Instead returned " + value);
            }

            if ((Boolean)value == true) {
                filteredList.add(payload);
            }
        }
        return filteredList;
    }
    
}
