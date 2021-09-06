package com.obj.nc.functions.processors.senders.mailchimp.dtos;

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotBlank;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class MailchimpRenderTemplateRequest {
    
    @NotBlank private String key;
    @NotBlank @JsonProperty("template_name") private String templateName;
    @JsonProperty("template_content") private List<MailchimpTemplateContentDto> templateContent;
    @JsonProperty("merge_vars") private List<MailchimpMergeVariableDto> mergeVars = new ArrayList<>();
    
}
