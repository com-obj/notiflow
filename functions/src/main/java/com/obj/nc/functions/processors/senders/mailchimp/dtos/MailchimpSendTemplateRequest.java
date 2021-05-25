package com.obj.nc.functions.processors.senders.mailchimp.dtos;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.obj.nc.domain.content.mailchimp.MailchimpContent;

import lombok.Data;

@Data
public class MailchimpSendTemplateRequest {
    
    @JsonProperty("key") private String key;
    @JsonProperty("dto") private MailchimpMessageDto message;
    @JsonProperty("template_name") private String templateName;
    @JsonProperty("template_content") private List<MailchimpTemplateContentDto> templateContent;
    
    public static MailchimpSendTemplateRequest from(MailchimpContent content, String authKey) {
        MailchimpSendTemplateRequest dto = new MailchimpSendTemplateRequest();
        dto.key = authKey;
    
        MailchimpMessageDto mailchimpMessageDto = new MailchimpMessageDto();
        mailchimpMessageDto.setAttachments(new ArrayList<>(content.getAttachments()));
        mailchimpMessageDto.setRecipients(content.getRecipients());
        mailchimpMessageDto.setFromName(content.getSenderName());
        mailchimpMessageDto.setFromEmail(content.getSenderEmail());
        mailchimpMessageDto.setSubject(content.getSubject());
        mailchimpMessageDto.setGlobalMergeVars(new ArrayList<>(content.getGlobalMergeVariables()));
        mailchimpMessageDto.setMergeLanguage(content.getMergeLanguage());
        dto.setMessage(mailchimpMessageDto);
    
        dto.setTemplateContent(new ArrayList<>(content.getTemplateContent()));
        dto.setTemplateName(content.getTemplateName());
        return dto;
    }
    
}
