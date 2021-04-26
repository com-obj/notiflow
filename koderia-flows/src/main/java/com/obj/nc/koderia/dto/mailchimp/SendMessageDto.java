package com.obj.nc.koderia.dto.mailchimp;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import lombok.Data;

@Data
public class SendMessageDto {

    @NotBlank
    private String key;

    @NotNull
    private MessageDto message;

}
