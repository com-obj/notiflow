package com.obj.nc.flows.emailFormattingAndSending;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties("nc.flows.email-processing")
public class EmailProcessingFlowProperties {
    
    private MULTI_LOCALES_MERGE_STRATEGY multiLocalesMergeStrategy = MULTI_LOCALES_MERGE_STRATEGY.MESSAGE_PER_LOCALE;
    
    public enum MULTI_LOCALES_MERGE_STRATEGY {
        MESSAGE_PER_LOCALE, MERGE
    }
    
}
