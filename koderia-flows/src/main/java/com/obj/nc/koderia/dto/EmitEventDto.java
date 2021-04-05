package com.obj.nc.koderia.dto;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Map;

@Data
public class EmitEventDto {

    @NotNull
    private Type type;

    @NotNull
    @Valid
    @JsonTypeInfo(
            use = JsonTypeInfo.Id.NAME,
            include = JsonTypeInfo.As.EXTERNAL_PROPERTY,
            property = "type",
            visible = true
    )
    @JsonSubTypes({
            @JsonSubTypes.Type(value = JobPostEventDataDto.class, name = "JOB_POST"),
            @JsonSubTypes.Type(value = BlogEventDataDto.class, name = "BLOG"),
            @JsonSubTypes.Type(value = EventEventDataDto.class, name = "EVENT"),
            @JsonSubTypes.Type(value = LinkEventDataDto.class, name = "LINK"),
            @JsonSubTypes.Type(value = NewsEventDataDto.class, name = "NEWS")
    })
    private EventDataDto data;

    @JsonIgnore
    public Map<String, Object> asMap() {
        return new ObjectMapper().convertValue(this, new TypeReference<Map<String, Object>>(){});
    }

    public enum Type {
        JOB_POST, BLOG, EVENT, LINK, NEWS
    }

}
