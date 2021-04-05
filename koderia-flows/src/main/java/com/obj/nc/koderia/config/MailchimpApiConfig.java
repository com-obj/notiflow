package com.obj.nc.koderia.config;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import com.obj.nc.koderia.dto.EmitEventDto;

import javax.validation.constraints.NotBlank;

@Validated
@Data
@Configuration
@ConfigurationProperties(prefix = "mailchimp")
public class MailchimpApiConfig {

    private MailchimpApiProperties api;

    private MailchimpTemplateProperties template;

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
        private String aggregateSubject;

        @NotBlank
        private String aggregateName;

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
