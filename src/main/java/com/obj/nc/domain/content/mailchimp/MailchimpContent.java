package com.obj.nc.domain.content.mailchimp;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.obj.nc.domain.content.Content;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@JsonTypeName(MailchimpContent.JSON_TYPE_IDENTIFIER)
public class MailchimpContent extends Content {
    
    public final static String JSON_TYPE_IDENTIFIER = "MAILCHIMP_CONTENT";
    public final static String DATA_MERGE_VARIABLE = "data";
    
    @EqualsAndHashCode.Include
    @NotNull private MailchimpMessage message;
    
    @EqualsAndHashCode.Include
    @NotBlank @JsonProperty("template_name") private String templateName;
    
    @EqualsAndHashCode.Include
    @JsonProperty("template_content") private List<MailchimpTemplateContent> templateContent = new ArrayList<>();
    
}
