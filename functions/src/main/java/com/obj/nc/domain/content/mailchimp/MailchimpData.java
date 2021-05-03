package com.obj.nc.domain.content.mailchimp;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.obj.nc.domain.Attachement;
import com.obj.nc.domain.IsTypedJson;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public abstract class MailchimpData implements IsTypedJson {
    
    @NotNull protected String type;
    
    public abstract <T> T getData();
    
    @JsonIgnore
    public abstract String getMessageSubject();
    
    @JsonIgnore
    public abstract String getMessageText();
    
    @JsonIgnore
    public abstract List<Attachement> getAttachments();
    
}
