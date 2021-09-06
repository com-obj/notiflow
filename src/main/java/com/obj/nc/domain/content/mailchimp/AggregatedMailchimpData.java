package com.obj.nc.domain.content.mailchimp;

import static com.obj.nc.domain.content.mailchimp.AggregatedMailchimpData.JSON_TYPE_NAME;

import java.util.List;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.obj.nc.domain.Attachement;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
@JsonTypeName(JSON_TYPE_NAME)
public class AggregatedMailchimpData extends MailchimpData {
    public static final String JSON_TYPE_NAME = "AGGREGATED";
    
    @NotNull private List<Object> data;
    @NotNull private String subject;
    @NotNull private List<Attachement> attachments;
    
}
