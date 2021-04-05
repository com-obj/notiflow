package com.obj.nc.koderia.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotBlank;

@Data
@EqualsAndHashCode(callSuper = true)
public class NewsEventDataDto extends EventDataDto {

    @NotBlank
    private String subject;

    @NotBlank
    private String text;

    @Override
    public String getMessageSubject() {
        return subject;
    }

    @Override
    public String getMessageText() {
        return text;
    }

}