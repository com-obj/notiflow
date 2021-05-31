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
    private String id;
    private String name;
    private String description;
    private String location;
    private String type;
    private String rate;
    private List<String> technologies;
    
    @JsonIgnore
    @Override
    public Object asMailchimpMergeVarContent() {
        Map<String, String> content = new HashMap<>();
        content.put("id", id);
        content.put("name", name);
        content.put("description", description);
        content.put("location", location);
        content.put("rate", rate);
        content.put("technologies", String.join(", ", technologies));
        return content;
    }
    
}

