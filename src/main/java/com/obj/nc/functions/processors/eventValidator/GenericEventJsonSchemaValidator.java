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
