package com.obj.nc.dto;

import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Data
public class SendSmsResponseDto {

    @NotNull
    @Valid
    private ResourceReferenceDto resourceReference;

}
