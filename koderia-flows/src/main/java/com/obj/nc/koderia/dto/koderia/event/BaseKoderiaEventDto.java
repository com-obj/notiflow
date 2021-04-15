package com.obj.nc.koderia.dto.koderia.event;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.obj.nc.domain.IsTypedJson;
import lombok.Data;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;
import java.util.Map;

@Data
@Validated
@JsonTypeInfo(include = JsonTypeInfo.As.EXISTING_PROPERTY, use = JsonTypeInfo.Id.NAME, property = "type", visible = true)
@JsonSubTypes({
        @JsonSubTypes.Type(value = JobPostKoderiaEventDto.class, name = "JOB_POST"),
        @JsonSubTypes.Type(value = BlogKoderiaEventDto.class, name = "BLOG"),
        @JsonSubTypes.Type(value = EventKoderiaEventDto.class, name = "EVENT"),
        @JsonSubTypes.Type(value = LinkKoderiaEventDto.class, name = "LINK"),
        @JsonSubTypes.Type(value = NewsKoderiaEventDto.class, name = "NEWS")
})
public abstract class BaseKoderiaEventDto implements IsTypedJson {
    public enum Type {
        JOB_POST, BLOG, EVENT, LINK, NEWS
    }
    
    @NotNull private Type type;
    
    @JsonIgnore
    public abstract String getMessageSubject();

    @JsonIgnore
    public abstract String getMessageText();
    
    @JsonIgnore
    public abstract String getTypeName();
    
    @JsonIgnore
    public abstract <T> T getData();
    
    @JsonIgnore
    public Map<String, Object> asMap() {
        return new ObjectMapper().convertValue(this, new TypeReference<Map<String, Object>>(){});
    }

}
