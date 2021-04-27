package com.obj.nc.osk.functions.processors.sms.dtos;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import lombok.Data;

@Data
public class OskSendSmsResponseDto {

    @NotNull
    @Valid
    private SendSmsResourceReferenceDto resourceReference;

}
