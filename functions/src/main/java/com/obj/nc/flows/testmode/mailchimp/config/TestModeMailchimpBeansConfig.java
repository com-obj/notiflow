package com.obj.nc.flows.testmode.mailchimp.config;

import com.obj.nc.flows.testmode.mailchimp.functions.InMemoryMailchimpSourceSupplier;
import com.obj.nc.flows.testmode.mailchimp.functions.TestModeMailchimpSender;
import com.obj.nc.functions.processors.senders.MailchimpSender;
import com.obj.nc.functions.processors.senders.mailchimp.MailchimpSenderConfigProperties;
import com.obj.nc.functions.processors.senders.mailchimp.MailchimpSenderProcessorFunction;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
@ConditionalOnProperty(value = "nc.flows.test-mode.enabled", havingValue = "true")
public class TestModeMailchimpBeansConfig {
	
    /**
     * The provided mailchimpSender service override. Instead of performing real Mailchimp delivery, this implementation sends message
     * to in memory store which is then used to create digest email
     * @param properties
     * @return
     */
    @Bean
    @Primary
    public MailchimpSender testModeMailchimpSender(MailchimpSenderConfigProperties properties) {
        return new TestModeMailchimpSender(properties, testModeMailchimpReciever(), realMailchimpSender());
    }
    
    @Bean
    @Primary
    public InMemoryMailchimpSourceSupplier testModeMailchimpReciever() {
        return new InMemoryMailchimpSourceSupplier();
    }
    
    public MailchimpSenderProcessorFunction realMailchimpSender() {
        return new MailchimpSenderProcessorFunction();
    }
    
}
