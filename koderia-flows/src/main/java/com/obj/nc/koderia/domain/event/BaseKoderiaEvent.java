package com.obj.nc.koderia.domain.event;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.obj.nc.domain.Attachement;
import com.obj.nc.domain.content.mailchimp.MailchimpData;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.List;


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
@EqualsAndHashCode(callSuper = true)
public abstract class BaseKoderiaEvent extends MailchimpData {
    @Override
    public List<Attachement> getAttachments() {
        return new ArrayList<>();
    }
}
