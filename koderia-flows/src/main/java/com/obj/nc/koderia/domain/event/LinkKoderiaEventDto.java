package com.obj.nc.koderia.domain.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.obj.nc.koderia.domain.eventData.LinkEventDataDto;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Data
@EqualsAndHashCode(callSuper = true)
public class LinkKoderiaEventDto extends BaseKoderiaEvent {
    @Valid @NotNull private LinkEventDataDto data;
    
    @Override
    public String getMessageSubject() {
        return data.getTitle();
    }
    
    @Override
    public String getMessageText() {
        return data.getDescription();
    }
}

