package com.obj.nc.koderia.dto.mailchimp;

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class MessageDto {

    @NotBlank
    private String subject;

    @NotBlank
    @JsonProperty("from_email")
    private String fromEmail;

    @NotBlank
    @JsonProperty("from_name")
    private String fromName;

    @NotEmpty
    private List<RecipientDto> to;

    @JsonProperty("merge_language")
    private String mergeLanguage = "handlebars";

    @JsonProperty("global_merge_vars")
    private List<MergeVarDto> globalMergeVars = new ArrayList<>();

    private List<AttachmentDto> attachments = new ArrayList<>();

}
