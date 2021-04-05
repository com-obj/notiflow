package com.obj.nc.koderia.services;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;

import com.obj.nc.koderia.config.KoderiaApiConfigProperties;
import com.obj.nc.koderia.config.RestClientConfig;
import com.obj.nc.koderia.mapper.RecipientMapperImpl;

@TestConfiguration
@Import({
        RecipientMapperImpl.class,
        RestClientConfig.class
})
@EnableConfigurationProperties(KoderiaApiConfigProperties.class)
public class KoderiaRestClientImplTestConfig {
}
