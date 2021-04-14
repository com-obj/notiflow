package com.obj.nc.koderia.functions.processors.recipientsFinder;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;

import com.obj.nc.koderia.mapper.RecipientMapperImpl;

@TestConfiguration
@Import({
        KoderiaRecipientsFinderProcessorFunction.class,
        RecipientMapperImpl.class
})
@EnableConfigurationProperties(KoderiaRecipientsFinderConfig.class)
class KoderiaRecipientsFinderProcessorFunctionTestConfig {
}