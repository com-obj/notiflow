package com.obj.nc.koderia.mapper;


import com.obj.nc.koderia.functions.processors.mailchimpSender.MailchimpSenderConfig;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;

@TestConfiguration
@Import({
        MailchimpMessageMapperAggregateImpl.class
})
@EnableConfigurationProperties(MailchimpSenderConfig.class)
class MailchimpMessageMapperAggregateImplTestConfig {
}