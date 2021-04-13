package com.obj.nc.koderia.functions.processors.senders;

import com.obj.nc.koderia.dto.EmitEventDto;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;

@Validated
@Data
@Configuration
@ConfigurationProperties(prefix = "koderia.functions.mailchimp-sender")
public class MailchimpSenderConfigProperties {
    
    private String aggregateSubject;

    private MailchimpApiProperties mailchimpApi;

    private MailchimpTemplateProperties mailchimpTemplateNames;

    @Getter
    @Setter
    public static class MailchimpApiProperties {
        @NotBlank
        private String url;

        @NotBlank
        private String authKey;

        @NotBlank
        private String senderName;

        @NotBlank
        private String senderEmail;
    }

    @Getter
    @Setter
    public static class MailchimpTemplateProperties {
        @NotBlank
        private String aggregate;

        @NotBlank
        private String jobPost;

        @NotBlank
        private String blog;

        @NotBlank
        private String event;

        @NotBlank
        private String link;

        @NotBlank
        private String news;
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

}
