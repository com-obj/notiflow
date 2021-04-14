package com.obj.nc.koderia.functions.processors.mailchimpSender;

import com.obj.nc.koderia.dto.EmitEventDto;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.client.RestTemplate;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Validated
@Data
@Configuration
@ConfigurationProperties(prefix = "koderia.functions.mailchimp-sender")
public class MailchimpSenderConfig {
    public static final String SEND_TEMPLATE_PATH = "/messages/send-template";
    public static final String MAILCHIMP_RESPONSE_FIELD = "mailchimpResponse";
    public static final String MAILCHIMP_REST_TEMPLATE = "mailchimpRestTemplate";
    
    
    @NotNull private String aggregateSubject;
    @Valid private MailchimpApiProperties mailchimpApi;
    @Valid private MailchimpTemplateProperties mailchimpTemplateNames;

    @Getter
    @Setter
    public static class MailchimpApiProperties {
        @NotBlank private String url;
        @NotBlank private String authKey;
        @NotBlank private String senderName;
        @NotBlank private String senderEmail;
    }

    @Getter
    @Setter
    public static class MailchimpTemplateProperties {
        @NotBlank private String aggregate;
        @NotBlank private String jobPost;
        @NotBlank private String blog;
        @NotBlank private String event;
        @NotBlank private String link;
        @NotBlank private String news;
    }

    public String getTemplateNameFromMessageType(EmitEventDto.Type messageType) {
        switch (messageType) {
            case JOB_POST:
                return mailchimpTemplateNames.jobPost;
            case BLOG:
                return mailchimpTemplateNames.blog;
            case EVENT:
                return mailchimpTemplateNames.event;
            case LINK:
                return mailchimpTemplateNames.link;
            case NEWS:
                return mailchimpTemplateNames.news;
            default:
                throw new IllegalArgumentException("Unknown message type");
        }
    }
    
    @Bean
    @Qualifier(MAILCHIMP_REST_TEMPLATE)
    public RestTemplate mailchimpRestTemplate(RestTemplateBuilder restTemplateBuilder) {
        return restTemplateBuilder
                .rootUri(mailchimpApi.url)
                .additionalInterceptors(
                        (httpRequest, bytes, clientHttpRequestExecution) -> {
                            httpRequest.getHeaders().add(HttpHeaders.AUTHORIZATION,
                                    "Bearer " + mailchimpApi.authKey);
                            return clientHttpRequestExecution.execute(httpRequest, bytes);
                        })
                .build();
    }
}
