package com.obj.nc.koderia.dto.koderia.data;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotBlank;

@Data
public class NewsEventDataDto {

    @NotBlank private String subject;
    @NotBlank private String text;

}
