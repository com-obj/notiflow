package com.obj.nc.koderia.domain.event;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.obj.nc.koderia.domain.eventData.EventEventDataDto;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import static com.obj.nc.koderia.domain.event.EventKoderiaEventDto.JSON_TYPE_NAME;

@Data
@EqualsAndHashCode(callSuper = true)
@JsonTypeName(JSON_TYPE_NAME)
public class EventKoderiaEventDto extends BaseKoderiaEvent {
    public static final String JSON_TYPE_NAME = "EVENT";
    
    @Valid @NotNull private EventEventDataDto data;
    
    @Override
    public String getMessageSubject() {
        return data.getName();
    }
}

