package com.obj.nc.domain.content.mailchimp;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Data
public class MailchimpMessage {

    @NotBlank private String subject;
    @NotBlank @JsonProperty("from_email") private String fromEmail;
    @NotBlank @JsonProperty("from_name") private String fromName;
    @NotEmpty private List<MailchimpRecipient> to;
    @JsonProperty("merge_language") private String mergeLanguage = "handlebars";
    @JsonProperty("global_merge_vars") private List<MailchimpMergeVariable> globalMergeVars = new ArrayList<>();
    private List<MailchimpAttachment> attachments = new ArrayList<>();
    
    public Optional<Object> findMergeVariableContentByName(String name) {
        return globalMergeVars.stream().filter(var -> name.equals(var.getName())).map(MailchimpMergeVariable::getContent).findFirst();
    }

}
