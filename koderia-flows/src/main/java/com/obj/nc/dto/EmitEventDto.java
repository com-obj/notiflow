package com.obj.nc.dto;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.Map;

@Data
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "type",
        visible = true
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = EmitJobPostEventDto.class, name = "JOB_POST"),
        @JsonSubTypes.Type(value = EmitBlogEventDto.class, name = "BLOG"),
        @JsonSubTypes.Type(value = EmitEventEventDto.class, name = "EVENT"),
        @JsonSubTypes.Type(value = EmitLinkEventDto.class, name = "LINK"),
        @JsonSubTypes.Type(value = EmitNewsEventDto.class, name = "NEWS")
})
public abstract class EmitEventDto {

    @NotNull
    private Type type;

    @NotNull
    private String subject;

    @NotNull
    private String text;

    @JsonIgnore
    public Map<String, Object> asMap() {
        return new ObjectMapper().convertValue(this, new TypeReference<Map<String, Object>>(){});
    }

    public enum Type {
        JOB_POST, BLOG, EVENT, LINK, NEWS
    }
}
