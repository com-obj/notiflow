package com.obj.nc.domain.content.mailchimp;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class MailchimpMessage {
    
    @JsonProperty("subject") private String subject;
    @JsonProperty("from_email") private String fromEmail;
    @JsonProperty("from_name") private String fromName;
    @JsonProperty("to") private List<MailchimpRecipient> recipients = new ArrayList<>();
    @JsonProperty("merge_language") private String mergeLanguage;
    @JsonProperty("global_merge_vars") private List<MailchimpMergeVariable> globalMergeVars = new ArrayList<>();
    @JsonProperty("attachments") private List<MailchimpAttachment> attachments = new ArrayList<>();

}
