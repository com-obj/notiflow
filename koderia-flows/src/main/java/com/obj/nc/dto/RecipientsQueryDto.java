package com.obj.nc.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "type",
        visible = true
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = JobPostRecipientsQueryDto.class, name = "JOB_POST"),
        @JsonSubTypes.Type(value = BlogRecipientsQueryDto.class, name = "BLOG"),
        @JsonSubTypes.Type(value = EventRecipientsQueryDto.class, name = "EVENT"),
        @JsonSubTypes.Type(value = LinkRecipientsQueryDto.class, name = "LINK"),
        @JsonSubTypes.Type(value = NewsRecipientsQueryDto.class, name = "NEWS")
})
@JsonIgnoreProperties(ignoreUnknown = true)
public class RecipientsQueryDto {

    @NotNull
    private EmitEventDto.Type type;

}

