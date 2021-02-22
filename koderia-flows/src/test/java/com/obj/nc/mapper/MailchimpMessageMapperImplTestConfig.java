package com.obj.nc.mapper;

import com.obj.nc.config.RestClientConfig;
import com.obj.nc.config.MailchimpApiConfig;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;

@TestConfiguration
@Import({
        MailchimpMessageMapperImpl.class,
        RestClientConfig.class
})
@EnableConfigurationProperties(MailchimpApiConfig.class)
class MailchimpMessageMapperImplTestConfig {
}