package com.obj.nc.koderia.mapper;


import com.obj.nc.functions.processors.senders.MailchimpSenderConfigProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;

import com.obj.nc.koderia.config.RestClientConfig;

@TestConfiguration
@Import({
        MailchimpMessageMapperAggregateImpl.class,
        RestClientConfig.class
})
@EnableConfigurationProperties(MailchimpSenderConfigProperties.class)
class MailchimpMessageMapperAggregateImplTestConfig {
}