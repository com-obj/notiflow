package com.obj.nc.functions.processors.senders.mailchimp;

import com.obj.nc.functions.processors.senders.MailchimpSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestTemplate;

@Configuration
public class MailchimpSenderConfig {
    
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
                            httpRequest.getHeaders().add(HttpHeaders.AUTHORIZATION,
                                    "Bearer " + properties.getAuthKey());
                            return clientHttpRequestExecution.execute(httpRequest, bytes);
                        })
                .build();
    }
    
    @Bean
    public MailchimpSender mailchimpSender() {
        return new MailchimpSenderProcessorFunction();
    }
    
}
