package com.obj.nc.functions.processors.senders.mailchimp.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.obj.nc.domain.content.mailchimp.MailchimpContent;
import com.obj.nc.domain.content.mailchimp.MailchimpMessage;
import com.obj.nc.domain.content.mailchimp.MailchimpTemplateContent;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class MailchimpContentDto {
    
    @NotBlank private String key;
    @NotNull private MailchimpMessage message;
    @NotBlank @JsonProperty("template_name") private String templateName;
    @JsonProperty("template_content") private List<MailchimpTemplateContent> templateContent;
    
    public static MailchimpContentDto from(MailchimpContent content) {
        MailchimpContentDto dto = new MailchimpContentDto();
        dto.key = content.getKey();
        dto.message = content.getMessage();
        dto.templateName = content.getTemplateName();
        dto.templateContent = content.getTemplateContent();
        return dto;
    }
    
}
