package com.obj.nc.koderia.functions.processors.senders;

import com.obj.nc.functions.processors.senders.MailchimpSenderConfigProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;

import com.obj.nc.koderia.config.RestClientConfig;
import com.obj.nc.koderia.mapper.MailchimpMessageMapperAggregateImpl;
import com.obj.nc.koderia.mapper.MailchimpMessageMapperImpl;
import com.obj.nc.koderia.services.MailchimpRestClientImpl;

@TestConfiguration
@Import({
        MailchimpSenderProcessingFunction.class,
        MailchimpSenderExecution.class,
        MailchimpSenderPreCondition.class,
        MailchimpMessageMapperImpl.class,
        MailchimpMessageMapperAggregateImpl.class,
        MailchimpRestClientImpl.class,
        RestClientConfig.class
})
@EnableConfigurationProperties(MailchimpSenderConfigProperties.class)
public class MailchimpSenderProcessingFunctionTestConfig {
}
