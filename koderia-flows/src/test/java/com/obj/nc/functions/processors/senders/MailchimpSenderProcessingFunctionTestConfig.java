package com.obj.nc.functions.processors.senders;

import com.obj.nc.config.BaseApiConfig;
import com.obj.nc.config.MailchimpApiConfig;
import com.obj.nc.mapper.MailchimpMessageMapperAggregateImpl;
import com.obj.nc.mapper.MailchimpMessageMapperImpl;
import com.obj.nc.services.MailchimpServiceRestImpl;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;

@TestConfiguration
@Import({
        MailchimpSenderProcessingFunction.class,
        MailchimpSenderExecution.class,
        MailchimpSenderPreCondition.class,
        MailchimpMessageMapperImpl.class,
        MailchimpMessageMapperAggregateImpl.class,
        MailchimpServiceRestImpl.class,
        BaseApiConfig.class
})
@EnableConfigurationProperties(MailchimpApiConfig.class)
public class MailchimpSenderProcessingFunctionTestConfig {
}
