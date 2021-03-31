package com.obj.nc.osk.dto;

import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Data
public class OskSendSmsResponseDto {

    @NotNull
    @Valid
    private SendSmsResourceReferenceDto resourceReference;

}
