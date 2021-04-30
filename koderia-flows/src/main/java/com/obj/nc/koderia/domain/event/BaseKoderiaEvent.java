package com.obj.nc.koderia.domain.event;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.obj.nc.domain.Attachement;
import com.obj.nc.domain.content.mailchimp.AggregatedMailchimpData;
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
        @JsonSubTypes.Type(value = JobPostKoderiaEventDto.class),
        @JsonSubTypes.Type(value = BlogKoderiaEventDto.class),
        @JsonSubTypes.Type(value = EventKoderiaEventDto.class),
        @JsonSubTypes.Type(value = LinkKoderiaEventDto.class),
        @JsonSubTypes.Type(value = NewsKoderiaEventDto.class),
        @JsonSubTypes.Type(value = AggregatedMailchimpData.class)
})
@EqualsAndHashCode(callSuper = true)
public abstract class BaseKoderiaEvent extends MailchimpData {
    @Override
    public List<Attachement> getAttachments() {
        return new ArrayList<>();
    }
}
