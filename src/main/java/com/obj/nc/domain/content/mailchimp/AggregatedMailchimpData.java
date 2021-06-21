package com.obj.nc.domain.content.mailchimp;

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
@JsonTypeName(JSON_TYPE_NAME)
public class AggregatedMailchimpData extends MailchimpData {
    public static final String JSON_TYPE_NAME = "AGGREGATED";
    
    @NotNull private List<MailchimpData> data;
    
    @Override
    public String getMessageSubject() {
        return getData().stream().map(var -> var.<MailchimpData>getData().getMessageSubject()).collect(Collectors.joining("/"));
    }
    
    @Override
    public String getMessageText() {
        return getData().stream().map(var -> var.<MailchimpData>getData().getMessageText()).collect(Collectors.joining("\n\n"));
    }
    
    @Override
    public List<Attachement> getAttachments() {
        return getData().stream().flatMap(var -> var.<MailchimpData>getData().getAttachments().stream()).collect(Collectors.toList());
    }
}
