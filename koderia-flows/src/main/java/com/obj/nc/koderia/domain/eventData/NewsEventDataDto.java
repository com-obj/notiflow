package com.obj.nc.koderia.domain.eventData;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class NewsEventDataDto {

    @NotBlank private String subject;
    @NotBlank private String text;

}
