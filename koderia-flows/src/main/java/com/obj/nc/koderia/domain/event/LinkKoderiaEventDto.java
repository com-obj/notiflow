package com.obj.nc.koderia.domain.event;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.obj.nc.koderia.domain.eventData.LinkEventDataDto;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import static com.obj.nc.koderia.domain.event.LinkKoderiaEventDto.JSON_TYPE_NAME;

@Data
@EqualsAndHashCode(callSuper = true)
@JsonTypeName(JSON_TYPE_NAME)
public class LinkKoderiaEventDto extends BaseKoderiaEvent {
    public static final String JSON_TYPE_NAME = "LINK";
    
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

