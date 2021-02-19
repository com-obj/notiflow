package com.obj.nc.mapper;


import com.obj.nc.config.BaseApiConfig;
import com.obj.nc.config.MailchimpApiConfig;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;

@TestConfiguration
@Import({
        MailchimpMessageMapperAggregateImpl.class,
        BaseApiConfig.class
})
@EnableConfigurationProperties(MailchimpApiConfig.class)
class MailchimpMessageMapperAggregateImplTestConfig {
}