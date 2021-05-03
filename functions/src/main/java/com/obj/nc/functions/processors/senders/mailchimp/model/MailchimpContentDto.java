package com.obj.nc.functions.processors.senders.mailchimp.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.obj.nc.domain.content.mailchimp.MailchimpContent;
import com.obj.nc.domain.content.mailchimp.MailchimpMessage;
import com.obj.nc.domain.content.mailchimp.MailchimpTemplateContent;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

@Data
public class MailchimpContentDto {
    
    @NotBlank private String key;
    @NotNull private MailchimpMessage message;
    @NotBlank @JsonProperty("template_name") private String templateName;
    @JsonProperty("template_content") private List<MailchimpTemplateContent> templateContent;
    
    public static MailchimpContentDto from(MailchimpContent content, String authKey) {
        MailchimpContentDto dto = new MailchimpContentDto();
        dto.key = authKey;
    
        MailchimpMessage mailchimpMessage = new MailchimpMessage();
        mailchimpMessage.setTo(new ArrayList<>(content.getMessage().getTo()));
        mailchimpMessage.setAttachments(new ArrayList<>(content.getMessage().getAttachments()));
        mailchimpMessage.setFromName(content.getMessage().getFromName());
        mailchimpMessage.setFromEmail(content.getMessage().getFromEmail());
        mailchimpMessage.setGlobalMergeVars(new ArrayList<>(content.getMessage().getGlobalMergeVars()));
        mailchimpMessage.setSubject(content.getMessage().getSubject());
        mailchimpMessage.setMergeLanguage(content.getMessage().getMergeLanguage());
        dto.setMessage(mailchimpMessage);
    
        dto.setTemplateContent(new ArrayList<>(content.getTemplateContent()));
        dto.setTemplateName(content.getTemplateName());
        return dto;
    }
    
}
