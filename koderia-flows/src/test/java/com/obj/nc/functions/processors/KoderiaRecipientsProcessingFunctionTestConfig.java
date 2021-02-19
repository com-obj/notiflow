package com.obj.nc.functions.processors;

import com.obj.nc.config.KoderiaApiConfig;
import com.obj.nc.mapper.RecipientMapperImpl;
import com.obj.nc.services.KoderiaServiceRestImpl;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@TestConfiguration
@Import({
        KoderiaRecipientsProcessingFunction.class,
        KoderiaRecipientsExecution.class,
        KoderiaRecipientsPreCondition.class,
        RecipientMapperImpl.class
})
@EnableConfigurationProperties(KoderiaApiConfig.class)
class KoderiaRecipientsProcessingFunctionTestConfig {
}