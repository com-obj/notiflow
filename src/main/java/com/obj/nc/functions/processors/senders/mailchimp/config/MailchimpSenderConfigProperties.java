package com.obj.nc.functions.processors.senders.mailchimp.config;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import lombok.Data;

@Validated
@Data
@Configuration
@ConfigurationProperties(prefix = "nc.functions.mailchimp-sender")
public class MailchimpSenderConfigProperties {
    
    private String apiUrl;
    private String authKey;
    private String senderEmail;
    private String mergeLanguage;
    private String aggregatedMessageSubject;
    private String aggregatedMessageTemplateName;
    private Map<String, String> mailchimpTemplateNames;
    
    public String getTemplateNameFromMessageType(String messageType) {
        if (mailchimpTemplateNames.containsKey(messageType)) {
            return mailchimpTemplateNames.get(messageType);
        }
        throw new IllegalArgumentException(String.format("Unknown message type: %s", messageType));
    }
    
    public Set<String> getMessageTypes() {
        return new HashSet<>(mailchimpTemplateNames.keySet());
    }
    
}
