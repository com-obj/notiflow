package com.obj.nc.domain.content.mailchimp;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.obj.nc.domain.HasRecievingEndpoints;
import com.obj.nc.domain.content.Content;
import com.obj.nc.domain.endpoints.MailchimpEndpoint;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@JsonTypeName(MailchimpContent.JSON_TYPE_IDENTIFIER)
public class MailchimpContent extends Content implements HasRecievingEndpoints {
    
    public final static String JSON_TYPE_IDENTIFIER = "MAILCHIMP_CONTENT";
    
    @EqualsAndHashCode.Include
    private MailchimpData originalEvent;
    private String subject;
    private String senderEmail;
    private String senderName;
    private List<MailchimpRecipient> recipients = new ArrayList<>();
    private List<MailchimpAttachment> attachments = new ArrayList<>();
    private String templateName;
    private List<MailchimpTemplateContent> templateContent = new ArrayList<>();
    private List<MailchimpMergeVariable> globalMergeVariables = new ArrayList<>();
    private String mergeLanguage;
    
	private List<MailchimpEndpoint> recievingEndpoints = new ArrayList<MailchimpEndpoint>();
    
}
