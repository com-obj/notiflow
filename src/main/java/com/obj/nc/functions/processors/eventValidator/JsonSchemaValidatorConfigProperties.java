package com.obj.nc.functions.processors.eventValidator;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

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