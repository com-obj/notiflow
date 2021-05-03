package com.obj.nc.domain.content.mailchimp;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Data
public class MailchimpMessage {

    @NotBlank private String subject;
    @NotBlank @JsonProperty("from_email") private String fromEmail;
    @NotBlank @JsonProperty("from_name") private String fromName;
    @NotEmpty private List<MailchimpRecipient> to = new ArrayList<>();
    @JsonProperty("merge_language") private String mergeLanguage = "handlebars";
    @JsonProperty("global_merge_vars") private List<MailchimpMergeVariable> globalMergeVars = new ArrayList<>();
    @NotNull private List<MailchimpAttachment> attachments = new ArrayList<>();
    
    @JsonIgnore
    public Optional<MailchimpData> getMailchimpData() {
        return globalMergeVars.stream().map(MailchimpMergeVariable::getContent).findFirst();
    }

}
