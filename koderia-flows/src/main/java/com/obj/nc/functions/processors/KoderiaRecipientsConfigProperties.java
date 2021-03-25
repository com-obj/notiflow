package com.obj.nc.functions.processors;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "koderia.functions.recipients")
public class KoderiaRecipientsConfigProperties {

    private String koderiaApiUrl;
    
    private String koderiaApiToken;

}
