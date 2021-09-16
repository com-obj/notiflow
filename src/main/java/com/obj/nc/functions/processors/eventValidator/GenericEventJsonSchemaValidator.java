/*
 *   Copyright (C) 2021 the original author or authors.
 *
 *   This file is part of Notiflow
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.obj.nc.functions.processors.eventValidator;

import java.io.IOException;
import java.util.Optional;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;

import com.obj.nc.domain.event.GenericEvent;
import com.obj.nc.exceptions.PayloadValidationException;
import com.obj.nc.functions.processors.ProcessorFunctionAdapter;
import com.obj.nc.utils.JsonUtils;

import io.restassured.module.jsv.JsonSchemaValidator;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class GenericEventJsonSchemaValidator extends ProcessorFunctionAdapter<GenericEvent, GenericEvent> {
    
    private final JsonSchemaValidatorConfigProperties properties;
    
    @Override
    protected Optional<PayloadValidationException> checkPreCondition(GenericEvent genericEvent) {
        Optional<PayloadValidationException> exception = super.checkPreCondition(genericEvent);
        if (exception.isPresent()) {
            return exception;
        }
    
        String payloadType = genericEvent.getPayloadType();
        
        if (payloadType == null) {
            return Optional.of(new PayloadValidationException(
                    String.format("GenericEvent %s does not contain required property \"payloadType\"", genericEvent)));
        }
    
        ClassLoader classLoader = this.getClass().getClassLoader();
        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(classLoader);
        
        boolean matchesSchema = false;
        try {
            Resource[] resources = resolver.getResources("classpath:" + properties.getJsonSchemaResourceDir() 
                    + "/" + properties.getJsonSchemaNameForPayloadType(payloadType) + ".json");
            if (resources.length == 0) {
                return Optional.of(new PayloadValidationException(
                        String.format("Could not find json schema for genericEvent type %s", payloadType)));
            }
            matchesSchema = JsonSchemaValidator.matchesJsonSchema(resources[0].getFile()).matches(JsonUtils.writeObjectToJSONString(genericEvent.getPayloadJson()));
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
        
        if (!matchesSchema) {
            return Optional.of(new PayloadValidationException(String.format("Payload %s does not match json schema of type %s", 
                    genericEvent.getPayloadJson(), payloadType)));
        }
        return Optional.empty();
    }
    
    @Override
    protected GenericEvent execute(GenericEvent genericEvent) {
        return genericEvent;
    }
    
}
