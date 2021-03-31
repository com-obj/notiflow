package com.obj.nc.osk.services.sms.dtos;

import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Data
public class OskSendSmsResponseDto {

    @NotNull
    @Valid
    private SendSmsResourceReferenceDto resourceReference;

}
