package com.obj.nc.flows.testmode.mailchimp.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class SendMessageDto {

    @NotBlank
    private String key;

    @NotNull
    private MessageDto message;

}
