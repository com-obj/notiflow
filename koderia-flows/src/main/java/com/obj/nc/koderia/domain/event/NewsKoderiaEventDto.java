package com.obj.nc.koderia.domain.event;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.obj.nc.koderia.domain.eventData.NewsEventDataDto;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import static com.obj.nc.koderia.domain.event.NewsKoderiaEventDto.JSON_TYPE_NAME;

@Data
@EqualsAndHashCode(callSuper = true)
@JsonTypeName(JSON_TYPE_NAME)
public class NewsKoderiaEventDto extends BaseKoderiaEvent {
    public static final String JSON_TYPE_NAME = "NEWS";
    
    @Valid @NotNull private NewsEventDataDto data;
    
    @Override
    public String getMessageSubject() {
        return data.getSubject();
    }
    
    @Override
    public String getMessageText() {
        return data.getText();
    }
}

