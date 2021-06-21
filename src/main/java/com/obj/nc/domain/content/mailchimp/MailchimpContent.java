package com.obj.nc.domain.content.mailchimp;

import java.util.ArrayList;
import java.util.List;

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
@Builder
public class MailchimpContent extends MessageContent {
    
    @EqualsAndHashCode.Include
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
    
}
