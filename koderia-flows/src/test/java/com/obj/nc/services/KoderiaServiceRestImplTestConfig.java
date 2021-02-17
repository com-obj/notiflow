package com.obj.nc.services;

import com.obj.nc.config.BaseApiConfig;
import com.obj.nc.config.KoderiaApiConfig;
import com.obj.nc.mapper.RecipientMapperImpl;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;

@TestConfiguration
@Import({
        KoderiaServiceRestImpl.class,
        RecipientMapperImpl.class,
        BaseApiConfig.class
})
@EnableConfigurationProperties(KoderiaApiConfig.class)
public class KoderiaServiceRestImplTestConfig {
}
