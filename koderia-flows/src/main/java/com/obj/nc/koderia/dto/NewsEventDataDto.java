package com.obj.nc.koderia.dto;

import javax.validation.constraints.NotBlank;

import lombok.Data;
import lombok.EqualsAndHashCode;

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
