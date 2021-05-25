package com.obj.nc.koderia.domain.eventData;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.obj.nc.functions.processors.senders.mailchimp.dtos.MailchimpMergeVariableDto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class JobPostEventDataDto extends BaseKoderiaData {
    
    @EqualsAndHashCode.Include
    @NotBlank private String id;
    @NotBlank private String name;
    @NotBlank private String description;
    @NotBlank private String location;
    @NotBlank private String rate;
    @NotEmpty private List<String> technologies;
    @NotBlank private String specialRate;
    @NotNull private List<String> labels;
    @NotEmpty private List<String> positionType;
    @NotBlank private String duration;
    @NotBlank private String type;
    @NotBlank private String dateOfStart;
    
    @JsonIgnore
    public String getPriorityRate() {
        return specialRate != null ? specialRate : rate.concat(" €");
    }
    
    @JsonIgnore
    @Override
    public Object asMailchimpMergeVarContent() {
        Map<String, String> content = new HashMap<>();
        content.put("id", id);
        content.put("name", name);
        content.put("description", description);
        content.put("location", location);
        content.put("rate", getPriorityRate());
        content.put("technologies", String.join(", ", technologies));
        return content;
    }
    
}

