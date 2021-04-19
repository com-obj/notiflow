package com.obj.nc.domain.content.mailchimp;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.obj.nc.domain.Attachement;
import com.obj.nc.domain.content.Content;
import com.obj.nc.domain.content.email.EmailContent;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@JsonTypeName(MailchimpContent.JSON_TYPE_IDENTIFIER)
public class MailchimpContent extends Content {
    
    public final static String JSON_TYPE_IDENTIFIER = "MAILCHIMP_CONTENT";
    public final static String DATA_MERGE_VARIABLE = "data";
    
    @NotNull private String subject;
    @NotNull private String text;
    @NotNull private List<Attachement> attachments = new ArrayList<Attachement>();
    
    @NotBlank private String key;
    @NotNull private MailchimpMessage message;
    @NotBlank @JsonProperty("template_name") private String templateName;
    @JsonProperty("template_content") private List<MailchimpTemplateContent> templateContent = new ArrayList<>();
    
    public EmailContent asSimpleEmailContent() {
        EmailContent emailContent = new EmailContent();
        emailContent.setSubject(subject);
        emailContent.setText(text);
        emailContent.setAttachments(attachments);
        return emailContent;
    }
    
}
