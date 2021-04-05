package com.obj.nc.koderia.functions.processors.senders;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;

import com.obj.nc.koderia.config.MailchimpApiConfig;
import com.obj.nc.koderia.config.RestClientConfig;
import com.obj.nc.koderia.functions.processors.senders.MailchimpSenderExecution;
import com.obj.nc.koderia.functions.processors.senders.MailchimpSenderPreCondition;
import com.obj.nc.koderia.functions.processors.senders.MailchimpSenderProcessingFunction;
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
@EnableConfigurationProperties(MailchimpApiConfig.class)
public class MailchimpSenderProcessingFunctionTestConfig {
}
