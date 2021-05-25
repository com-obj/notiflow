package com.obj.nc.koderia.functions.processors.eventValidator;

import com.obj.nc.exceptions.PayloadValidationException;
import com.obj.nc.functions.processors.eventValidator.GenericEventValidator;
import io.restassured.module.jsv.JsonSchemaValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

@Primary
@Component
@RequiredArgsConstructor
public class KoderiaGenericEventValidator extends GenericEventValidator {
    
    private final KoderiaGenericEventValidatorConfigProperties properties;
    
    @Override
    protected Optional<PayloadValidationException> checkPreCondition(String payload) {
        Optional<PayloadValidationException> exception = super.checkPreCondition(payload);
        if (exception.isPresent()) {
            return exception;
        }
    
        ClassLoader classLoader = this.getClass().getClassLoader();
        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(classLoader);
    
        Optional<Resource> firstSchemaMatch = Optional.empty();
        try {
            Resource[] resources = resolver.getResources("classpath:"+properties.getJsonSchemaDirPath()+"/**");
            firstSchemaMatch = Arrays.stream(resources).filter(Resource::isFile).filter(resource -> {
                try {
                    return JsonSchemaValidator.matchesJsonSchema(resource.getFile()).matches(payload);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }).findFirst();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    
        if (!firstSchemaMatch.isPresent()) {
            return Optional.of(new PayloadValidationException("Koderia event json does not match any known json schema"));
        }
        
        return Optional.empty();
    }
    
}
