package com.obj.nc.osk.functions.processors.sms.dtos;

import javax.validation.constraints.NotBlank;

import lombok.Data;

@Data
public class SendSmsResourceReferenceDto {

    @NotBlank
    private String resourceURL;

}
