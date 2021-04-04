package com.obj.nc.osk.functions.processors.sms.dtos;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class SendSmsResourceReferenceDto {

    @NotBlank
    private String resourceURL;

}
