package com.obj.nc.koderia.functions.processors.recipientsFinder;

import com.obj.nc.functions.processors.senders.mailchimp.MailchimpSenderConfig;
import com.obj.nc.functions.processors.senders.mailchimp.MailchimpSenderConfigProperties;
import com.obj.nc.koderia.functions.processors.eventConverter.KoderiaEventConverterConfig;
import com.obj.nc.koderia.functions.processors.eventConverter.KoderiaEventConverter;
import com.obj.nc.mappers.MailchimpDataToMailchimpContentMapper;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;

import com.obj.nc.koderia.mapper.KoderiaRecipientsMapper;

@TestConfiguration
@Import({
        KoderiaRecipientsFinder.class,
        KoderiaRecipientsMapper.class,
        KoderiaEventConverter.class,
        MailchimpDataToMailchimpContentMapper.class,
        MailchimpSenderConfig.class,
        MailchimpSenderConfigProperties.class,
        KoderiaEventConverterConfig.class
})
@EnableConfigurationProperties(KoderiaRecipientsFinderConfig.class)
class KoderiaRecipientsFinderProcessorFunctionTestConfig {
}