package com.obj.nc.koderia.functions.processors.mailchimpSender;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;

import com.obj.nc.koderia.mapper.MailchimpMessageMapperAggregateImpl;
import com.obj.nc.koderia.mapper.MailchimpMessageMapperImpl;

@TestConfiguration
@Import({
        MailchimpSenderConfig.class,
        MailchimpMessageMapperImpl.class,
        MailchimpMessageMapperAggregateImpl.class
})
public class MailchimpSenderProcessorFunctionTestConfig {
}
