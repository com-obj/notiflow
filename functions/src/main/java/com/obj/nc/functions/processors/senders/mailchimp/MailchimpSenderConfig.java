package com.obj.nc.functions.processors.senders.mailchimp;

import com.obj.nc.functions.processors.senders.MailchimpSender;
import lombok.Data;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.client.RestTemplate;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.*;

@Validated
@Data
@Configuration
@ConfigurationProperties(prefix = "nc.functions.mailchimp-sender")
public class MailchimpSenderConfig {
    
    public static final String SEND_TEMPLATE_PATH = "/messages/send-template";
    public static final String MAILCHIMP_RESPONSE_FIELD = "mailchimpResponse";
    public static final String MAILCHIMP_REST_TEMPLATE = "mailchimpRestTemplate";
    
    private String apiUrl;
    private String authKey;
    private String senderEmail;
    private String senderName;
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
    
    @Bean
    @Qualifier(MAILCHIMP_REST_TEMPLATE)
    public RestTemplate mailchimpRestTemplate(RestTemplateBuilder restTemplateBuilder) {
        return restTemplateBuilder
                .rootUri(apiUrl)
                .additionalInterceptors(
                        (httpRequest, bytes, clientHttpRequestExecution) -> {
                            httpRequest.getHeaders().add(HttpHeaders.AUTHORIZATION,
                                    "Bearer " + authKey);
                            return clientHttpRequestExecution.execute(httpRequest, bytes);
                        })
                .build();
    }
    
    @Bean
    public MailchimpSender mailchimpSender() {
        return new MailchimpSenderProcessorFunction();
    }
    
}
