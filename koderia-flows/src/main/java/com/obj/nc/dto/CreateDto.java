package com.obj.nc.dto;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;

import java.util.Map;

@Data
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "type",
        visible = true
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = CreateJobPostDto.class, name = "JOB_POST"),
        @JsonSubTypes.Type(value = CreateBlogDto.class, name = "BLOG"),
        @JsonSubTypes.Type(value = CreateEventDto.class, name = "EVENT"),
        @JsonSubTypes.Type(value = CreateLinkDto.class, name = "LINK"),
        @JsonSubTypes.Type(value = CreateNewsDto.class, name = "NEWS")
})
public class CreateDto {

    private Type type;

    private String subject;

    private String text;

    @JsonIgnore
    public Map<String, Object> asMap() {
        return new ObjectMapper().convertValue(this, new TypeReference<Map<String, Object>>(){});
    }

    public enum Type {
        JOB_POST, BLOG, EVENT, LINK, NEWS
    }
}
