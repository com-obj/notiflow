package com.obj.nc.functions.processors.eventValidator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.obj.nc.exceptions.PayloadValidationException;
import com.obj.nc.functions.processors.ProcessorFunctionAdapter;
import com.obj.nc.utils.JsonUtils;
import io.restassured.module.jsv.JsonSchemaValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class GenericEventJsonSchemaValidator extends ProcessorFunctionAdapter<JsonNode, JsonNode> {
    
    private final JsonSchemaValidatorConfigProperties properties;
    
    @Override
    protected Optional<PayloadValidationException> checkPreCondition(JsonNode payload) {
        Optional<PayloadValidationException> exception = super.checkPreCondition(payload);
        if (exception.isPresent()) {
            return exception;
        }
    
        JsonNode payloadType = payload.get("payloadType");
        ObjectNode payloadAsObjectNode = (ObjectNode) payload;
        payloadAsObjectNode.remove("payloadType");
        
        if (payloadType == null) {
            return Optional.of(new PayloadValidationException(
                    String.format("Payload %s does not contain required property \"payloadType\"", JsonUtils.writeObjectToJSONString(payload))));
        }
    
        ClassLoader classLoader = this.getClass().getClassLoader();
        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(classLoader);
        
        boolean matchesSchema = false;
        try {
            Resource[] resources = resolver.getResources("classpath:" + properties.getJsonSchemaResourceDir() 
                    + "/" + properties.getJsonSchemaNameForPayloadType(payloadType.textValue()) + ".json");
            if (resources.length == 0) {
                return Optional.of(new PayloadValidationException(
                        String.format("Could not find json schema for payload type %s", payloadType.textValue())));
            }
            matchesSchema = JsonSchemaValidator.matchesJsonSchema(resources[0].getFile()).matches(JsonUtils.writeObjectToJSONString(payload));
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
        
        if (!matchesSchema) {
            return Optional.of(new PayloadValidationException(String.format("Payload %s does not match json schema of type %s", 
                    JsonUtils.writeObjectToJSONString(payload), payloadType.textValue())));
        }
        return Optional.empty();
    }
    
    @Override
    protected JsonNode execute(JsonNode payload) {
        return payload;
    }
    
}
