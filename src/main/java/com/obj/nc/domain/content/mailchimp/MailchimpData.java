package com.obj.nc.domain.content.mailchimp;

import java.util.List;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.obj.nc.domain.Attachement;
import com.obj.nc.domain.IsTypedJson;

import lombok.Data;

@Data
public abstract class MailchimpData implements IsTypedJson {
    
    @NotNull protected String type;
    
    public abstract <T> T getData();
    
    @JsonIgnore
    public abstract String getSubject();
    
    @JsonIgnore
    public abstract List<Attachement> getAttachments();
    
}
