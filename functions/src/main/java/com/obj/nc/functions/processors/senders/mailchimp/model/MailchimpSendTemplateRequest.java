package com.obj.nc.functions.processors.senders.mailchimp.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.obj.nc.domain.content.mailchimp.MailchimpContent;
import com.obj.nc.domain.content.mailchimp.MailchimpMessage;
import com.obj.nc.domain.content.mailchimp.MailchimpTemplateContent;
import com.obj.nc.functions.processors.senders.mailchimp.MailchimpSenderConfigProperties;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class MailchimpSendTemplateRequest {
    
    @JsonProperty("key") private String key;
    @JsonProperty("message") private MailchimpMessage message;
    @JsonProperty("template_name") private String templateName;
    @JsonProperty("template_content") private List<MailchimpTemplateContent> templateContent;
    
    public static MailchimpSendTemplateRequest from(MailchimpContent content, String authKey) {
        MailchimpSendTemplateRequest dto = new MailchimpSendTemplateRequest();
        dto.key = authKey;
    
        MailchimpMessage mailchimpMessage = new MailchimpMessage();
        mailchimpMessage.setAttachments(new ArrayList<>(content.getAttachments()));
        mailchimpMessage.setFromName(content.getSenderName());
        mailchimpMessage.setFromEmail(content.getSenderEmail());
        mailchimpMessage.setSubject(content.getSubject());
        mailchimpMessage.setGlobalMergeVars(new ArrayList<>(content.getGlobalMergeVariables()));
        mailchimpMessage.setMergeLanguage(content.getMergeLanguage());
        dto.setMessage(mailchimpMessage);
    
        dto.setTemplateContent(new ArrayList<>(content.getTemplateContent()));
        dto.setTemplateName(content.getTemplateName());
        return dto;
    }
    
}
