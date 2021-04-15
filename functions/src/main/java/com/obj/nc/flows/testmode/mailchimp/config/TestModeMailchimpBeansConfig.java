package com.obj.nc.flows.testmode.mailchimp.config;

import com.obj.nc.flows.testmode.mailchimp.functions.InMemoryMailchimpSourceSupplier;
import com.obj.nc.flows.testmode.mailchimp.functions.TestModeMailchimpSender;
import com.obj.nc.functions.processors.senders.MailchimpSender;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
@ConditionalOnProperty(value = "nc.flows.test-mode.enabled", havingValue = "true")
@ConditionalOnBean(MailchimpSender.class)
public class TestModeMailchimpBeansConfig {
	
    /**
     * The provided mailchimpSender service override. Instead of performing real Mailchimp delivery, this implementation sends message
     * to in memory store which is then used to create digest email
     * @param properties
     * @return
     */
    @Bean
    @Primary
    public MailchimpSender testModeMailchimpSender() {
        return new TestModeMailchimpSender(mailchimpReciever());
    }
    
    @Bean
    public InMemoryMailchimpSourceSupplier mailchimpReciever() {
        return new InMemoryMailchimpSourceSupplier();
    }
    
}
