package com.obj.nc.koderia.dto.mailchimp;

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
