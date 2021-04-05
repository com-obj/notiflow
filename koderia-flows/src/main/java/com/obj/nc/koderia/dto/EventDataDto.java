package com.obj.nc.koderia.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class EventDataDto {

    @JsonIgnore
    public abstract String getMessageSubject();

    @JsonIgnore
    public abstract String getMessageText();

}
