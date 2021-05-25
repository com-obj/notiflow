package com.obj.nc.functions.processors.senders.mailchimp.dtos;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class MailchimpMessageDto {
    
    @JsonProperty("subject") private String subject;
    @JsonProperty("from_email") private String fromEmail;
    @JsonProperty("from_name") private String fromName;
    @JsonProperty("to") private List<MailchimpRecipientDto> recipients = new ArrayList<>();
    @JsonProperty("merge_language") private String mergeLanguage;
    @JsonProperty("global_merge_vars") private List<MailchimpMergeVariableDto> globalMergeVars = new ArrayList<>();
    @JsonProperty("attachments") private List<MailchimpAttachmentDto> attachments = new ArrayList<>();

}
