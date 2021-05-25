package com.obj.nc.koderia.functions.processors.recipientsFinder;

import com.obj.nc.functions.processors.senders.mailchimp.MailchimpSenderConfig;
import com.obj.nc.functions.processors.senders.mailchimp.MailchimpSenderConfigProperties;
import com.obj.nc.components.MailchimpContentFactoryImpl;
import com.obj.nc.functions.processors.eventFactory.MailchimpEventConverter;
import com.obj.nc.koderia.config.DomainConfig;
import com.obj.nc.koderia.mapper.KoderiaMergeVarMapperImpl;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;

@TestConfiguration
@Import({
        KoderiaRecipientsFinder.class,
        MailchimpEventConverter.class,
        KoderiaMergeVarMapperImpl.class,
        MailchimpContentFactoryImpl.class,
        MailchimpSenderConfig.class,
        MailchimpSenderConfigProperties.class,
        DomainConfig.class
})
@EnableConfigurationProperties(KoderiaRecipientsFinderConfig.class)
class KoderiaRecipientsFinderProcessorFunctionTestConfig {
}