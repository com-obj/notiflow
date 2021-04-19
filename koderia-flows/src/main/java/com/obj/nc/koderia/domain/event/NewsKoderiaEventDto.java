package com.obj.nc.koderia.domain.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.obj.nc.koderia.domain.eventData.NewsEventDataDto;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Data
@EqualsAndHashCode(callSuper = true)
public class NewsKoderiaEventDto extends BaseKoderiaEvent {
    @JsonProperty("data")
    @Valid @NotNull private NewsEventDataDto data;
    
    @Override
    public String getMessageSubject() {
        return data.getSubject();
    }
    
    @Override
    public String getMessageText() {
        return data.getText();
    }
    
    @Override
    public String getTypeName() {
        return "NEWS";
    }
}

