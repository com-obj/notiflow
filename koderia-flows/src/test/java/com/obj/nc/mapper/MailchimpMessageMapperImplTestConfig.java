package com.obj.nc.mapper;

import com.obj.nc.config.RestApiConfig;
import com.obj.nc.config.MailchimpApiConfig;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;

@TestConfiguration
@Import({
        MailchimpMessageMapperImpl.class,
        RestApiConfig.class
})
@EnableConfigurationProperties(MailchimpApiConfig.class)
class MailchimpMessageMapperImplTestConfig {
}