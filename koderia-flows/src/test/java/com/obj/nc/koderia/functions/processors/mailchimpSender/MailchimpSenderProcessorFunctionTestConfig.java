package com.obj.nc.koderia.functions.processors.mailchimpSender;

import com.obj.nc.functions.processors.senders.mailchimp.MailchimpSenderConfig;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;

import com.obj.nc.koderia.mapper.MailchimpAggregatedContentMapper;
import com.obj.nc.koderia.mapper.KoderiaEvent2MailchimpContentMapper;

@TestConfiguration
@Import({
        MailchimpSenderConfig.class,
        KoderiaEvent2MailchimpContentMapper.class,
        MailchimpAggregatedContentMapper.class
})
public class MailchimpSenderProcessorFunctionTestConfig {
}
