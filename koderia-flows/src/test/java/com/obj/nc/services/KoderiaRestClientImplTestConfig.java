package com.obj.nc.services;

import com.obj.nc.config.RestClientConfig;
import com.obj.nc.config.KoderiaApiConfigProperties;
import com.obj.nc.mapper.RecipientMapperImpl;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;

@TestConfiguration
@Import({
        RecipientMapperImpl.class,
        RestClientConfig.class
})
@EnableConfigurationProperties(KoderiaApiConfigProperties.class)
public class KoderiaRestClientImplTestConfig {
}
