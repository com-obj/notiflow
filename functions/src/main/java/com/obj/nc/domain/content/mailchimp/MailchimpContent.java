package com.obj.nc.domain.content.mailchimp;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.obj.nc.domain.content.MessageContent;
import com.obj.nc.functions.processors.senders.mailchimp.dtos.MailchimpAttachmentDto;
import com.obj.nc.functions.processors.senders.mailchimp.dtos.MailchimpMergeVariableDto;
import com.obj.nc.functions.processors.senders.mailchimp.dtos.MailchimpRecipientDto;
import com.obj.nc.functions.processors.senders.mailchimp.dtos.MailchimpTemplateContentDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@JsonTypeName(MailchimpContent.JSON_TYPE_IDENTIFIER)
@Builder
public class MailchimpContent extends MessageContent {
    
    public final static String JSON_TYPE_IDENTIFIER = "MAILCHIMP_CONTENT";
    
    @EqualsAndHashCode.Include
    //TODO: this is not a content, it should be part of the message
    private MailchimpData originalEvent;
    private String subject;
    //TODO: this is not a content, it should be part of the message    
    private String senderEmail;
    //TODO: this is not a content, it should be part of the message    
    private String senderName;
    @Builder.Default
    //TODO: this is not a content, it should be part of the message
    private List<MailchimpRecipientDto> recipients = new ArrayList<>();
    @Builder.Default
    private List<MailchimpAttachmentDto> attachments = new ArrayList<>();
    private String templateName;
    @Builder.Default
    private List<MailchimpTemplateContentDto> templateContent = new ArrayList<>();
    @Builder.Default
    private List<MailchimpMergeVariableDto> globalMergeVariables = new ArrayList<>();
    private String mergeLanguage;
    
    @Override
    public String getContentTypeName() {
    	return JSON_TYPE_IDENTIFIER;
    }
    
}
