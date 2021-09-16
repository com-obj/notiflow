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

import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Data
@Configuration
@ConfigurationProperties("nc.functions.json-schema-validator")
public class JsonSchemaValidatorConfigProperties {
    
    private String jsonSchemaResourceDir;
    private Map<String, String> payloadTypeJsonSchemaName;
    
    public String getJsonSchemaNameForPayloadType(String payloadType) {
        if (payloadTypeJsonSchemaName.containsKey(payloadType)) {
            return payloadTypeJsonSchemaName.get(payloadType);
        }
        throw new IllegalArgumentException(String.format("Unknown message type: %s", payloadType));
    }
    
}
