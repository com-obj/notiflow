package com.obj.nc.koderia.mapper;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;

import com.obj.nc.koderia.config.MailchimpApiConfig;
import com.obj.nc.koderia.config.RestClientConfig;

@TestConfiguration
@Import({
        MailchimpMessageMapperImpl.class,
        RestClientConfig.class
})
@EnableConfigurationProperties(MailchimpApiConfig.class)
class MailchimpMessageMapperImplTestConfig {
}