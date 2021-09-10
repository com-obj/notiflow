package com.obj.nc.functions.processors.senders.mailchimp.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;


@Configuration
public class MailchimpSenderConfig {
    
    public static final String SEND_PATH = "/messages/send";
    public static final String SEND_TEMPLATE_PATH = "/messages/send-template";
    public static final String MAILCHIMP_RESPONSE_FIELD = "mailchimpResponse";
    public static final String MAILCHIMP_REST_TEMPLATE = "mailchimpRestTemplate";
    
    @Autowired private MailchimpSenderConfigProperties properties;
    
    @Bean
    @Qualifier(MAILCHIMP_REST_TEMPLATE)
    public RestTemplate mailchimpRestTemplate(RestTemplateBuilder restTemplateBuilder) {
        return restTemplateBuilder
                .rootUri(properties.getApiUrl())
                .additionalInterceptors(
                        (httpRequest, bytes, clientHttpRequestExecution) -> {
                            httpRequest.getHeaders().setBearerAuth(properties.getAuthKey());
                            return clientHttpRequestExecution.execute(httpRequest, bytes);
                        })
                .build();
    }
    
}
