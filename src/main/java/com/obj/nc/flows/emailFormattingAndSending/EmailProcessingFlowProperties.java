package com.obj.nc.flows.emailFormattingAndSending;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Data
@Configuration
@ConfigurationProperties("nc.flows.email-processing")
public class EmailProcessingFlowProperties {
    
    private MULTI_LOCALES_MERGE_STRATEGY multiLocalesMergeStrategy = MULTI_LOCALES_MERGE_STRATEGY.MESSAGE_PER_LOCALE;
    
    public enum MULTI_LOCALES_MERGE_STRATEGY {
        MESSAGE_PER_LOCALE, MERGE
    }
    
}
