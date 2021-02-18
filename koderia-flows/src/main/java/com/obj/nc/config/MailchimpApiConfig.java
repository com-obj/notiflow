package com.obj.nc.config;

import com.obj.nc.dto.EmitEventDto;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import javax.validation.constraints.NotBlank;

@Validated
@Data
@Configuration
@ConfigurationProperties(prefix = "mailchimp")
public class MailchimpApiConfig {

    public static final String MAILCHIMP_REST_TEMPLATE = "mailchimpRestTemplate";

    private MailchimpApiProperties api;

    private MailchimpTemplateProperties template;

    @Autowired
    private ResponseErrorHandler responseErrorHandler;

    @Bean(MAILCHIMP_REST_TEMPLATE)
    public RestTemplate mailchimpRestTemplate() {
        return new RestTemplateBuilder()
                .rootUri(api.uri)
                .errorHandler(responseErrorHandler)
                .build();
    }

    @Getter
    @Setter
    public static class MailchimpApiProperties {
        @NotBlank
        private String uri;

        @NotBlank
        private String key;

        @NotBlank
        private String fromName;

        @NotBlank
        private String fromEmail;
    }

    @Getter
    @Setter
    public static class MailchimpTemplateProperties {
        @NotBlank
        private String aggregateName;

        @NotBlank
        private String aggregateSubject;

        @NotBlank
        private String jobPostName;

        @NotBlank
        private String blogName;

        @NotBlank
        private String eventName;

        @NotBlank
        private String linkName;

        @NotBlank
        private String newsName;
    }

    public String getTemplateNameFromMessageType(EmitEventDto.Type messageType) {
        switch (messageType) {
            case JOB_POST:
                return template.jobPostName;
            case BLOG:
                return template.blogName;
            case EVENT:
                return template.eventName;
            case LINK:
                return template.linkName;
            case NEWS:
                return template.newsName;
            default:
                throw new IllegalArgumentException("Unknown message type");
        }
    }

}
