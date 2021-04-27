package com.obj.nc.koderia.domain.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.obj.nc.koderia.domain.eventData.BaseKoderiaData;
import com.obj.nc.koderia.domain.eventData.BlogEventDataDto;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Data
@EqualsAndHashCode(callSuper = true)
public class BlogKoderiaEventDto extends BaseKoderiaEvent {
    @Valid @NotNull private BlogEventDataDto data;
    
    @Override
    public String getMessageSubject() {
        return data.getTitle();
    }
    
    @Override
    public String getMessageText() {
        return data.getContent();
    }
}

