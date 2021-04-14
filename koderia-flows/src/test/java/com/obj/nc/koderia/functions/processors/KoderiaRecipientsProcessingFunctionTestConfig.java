package com.obj.nc.koderia.functions.processors;

import com.obj.nc.koderia.functions.processors.recipientsFinder.KoderiaRecipientsFinderConfig;
import com.obj.nc.koderia.functions.processors.recipientsFinder.KoderiaRecipientsFinderProcessorFunction;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;

import com.obj.nc.koderia.config.RestClientConfig;
import com.obj.nc.koderia.mapper.RecipientMapperImpl;

@TestConfiguration
@Import({
        KoderiaRecipientsFinderProcessorFunction.class,
        KoderiaRecipientsExecution.class,
        KoderiaRecipientsPreCondition.class,
        RecipientMapperImpl.class,
        RestClientConfig.class
})
@EnableConfigurationProperties(KoderiaRecipientsFinderConfig.class)
class KoderiaRecipientsProcessingFunctionTestConfig {
}