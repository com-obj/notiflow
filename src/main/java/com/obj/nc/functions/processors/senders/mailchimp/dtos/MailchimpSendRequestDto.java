package com.obj.nc.functions.processors.senders.mailchimp.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.NotNull;
import java.util.List;


@Data
@SuperBuilder
public class MailchimpSendRequestDto {
    
    @NotNull
    @JsonProperty("key")
    protected String key;
    
    @NotNull
    @JsonProperty("message")
    protected MailchimpMessageDto message;
    
    @JsonProperty("template_name")
    private String templateName;
    
    @JsonProperty("template_content")
    private List<MailchimpTemplateContentDto> templateContent;
    
}
