package com.obj.nc.koderia.domain.event;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.obj.nc.koderia.domain.eventData.BlogEventDataDto;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import static com.obj.nc.koderia.domain.event.BlogKoderiaEventDto.JSON_TYPE_NAME;

@Data
@EqualsAndHashCode(callSuper = true)
@JsonTypeName(JSON_TYPE_NAME)
public class BlogKoderiaEventDto extends BaseKoderiaEvent {
    public static final String JSON_TYPE_NAME = "BLOG";
    
    @Valid @NotNull private BlogEventDataDto data;
    
    @Override
    public String getSubject() {
        return data.getTitle();
    }
}

