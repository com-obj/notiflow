package com.obj.nc.functions.processors.senders.mailchimp.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MailchimpMessageDto {
    
    @JsonProperty("subject") 
    private String subject;
    
    @JsonProperty("html")
    private String html;
    
    @JsonProperty("from_email") 
    private String fromEmail;
    
    @JsonProperty("from_name") 
    private String fromName;
    
    @JsonProperty("to") 
    private List<MailchimpRecipientDto> recipients;
    
    @JsonProperty("merge_language") 
    private String mergeLanguage;
    
    @JsonProperty("global_merge_vars") 
    private List<MailchimpMergeVariableDto> globalMergeVars;
    
    @JsonProperty("attachments") 
    private List<MailchimpAttachmentDto> attachments;

}
