package com.obj.nc.domain.content.mailchimp;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.obj.nc.domain.Attachement;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.stream.Collectors;

import static com.obj.nc.domain.content.mailchimp.AggregatedMailchimpData.JSON_TYPE_NAME;

@Data
@EqualsAndHashCode(callSuper = false)
@JsonTypeInfo(include = JsonTypeInfo.As.EXISTING_PROPERTY, use = JsonTypeInfo.Id.NAME, property = "type", visible = true)
@JsonSubTypes({
        @JsonSubTypes.Type(value = AggregatedMailchimpData.class),
})
@JsonTypeName(JSON_TYPE_NAME)
public class AggregatedMailchimpData extends MailchimpData {
    public static final String JSON_TYPE_NAME = "AGGREGATED";
    
    @NotNull private List<MailchimpMergeVariable> data;
    
    @Override
    public String getMessageSubject() {
        return getData().stream().map(var -> var.getContent().getMessageSubject()).collect(Collectors.joining("/"));
    }
    
    @Override
    public String getMessageText() {
        return getData().stream().map(var -> var.getContent().getMessageText()).collect(Collectors.joining("\n\n"));
    }
    
    @Override
    public List<Attachement> getAttachments() {
        return getData().stream().flatMap(var -> var.getContent().getAttachments().stream()).collect(Collectors.toList());
    }
}
