package com.obj.nc.koderia.functions.processors.eventValidator;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties("koderia.functions.generic-event-validator")
public class KoderiaGenericEventValidatorConfigProperties {
    
    private String jsonSchemaDirPath;
    
}
