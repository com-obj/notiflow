package com.obj.nc.koderia.mapper;

import com.obj.nc.functions.processors.senders.mailchimp.MailchimpSenderConfig;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;

@TestConfiguration
@Import({
        MailchimpSenderConfig.class,
        KoderiaEvent2MailchimpContentMapper.class,
        MailchimpAggregatedContentMapper.class
})
class MailchimpMessageMapperImplTestConfig {
}