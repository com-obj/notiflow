package com.obj.nc.koderia.domain.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.obj.nc.koderia.domain.eventData.EventEventDataDto;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Data
@EqualsAndHashCode(callSuper = true)
public class EventKoderiaEventDto extends BaseKoderiaEvent {
    @JsonProperty("data")
    @Valid @NotNull private EventEventDataDto data;
    
    @Override
    public String getMessageSubject() {
        return data.getName();
    }
    
    @Override
    public String getMessageText() {
        return data.getDescription();
    }
    
    @Override
    public String getTypeName() {
        return "EVENT";
    }
}

