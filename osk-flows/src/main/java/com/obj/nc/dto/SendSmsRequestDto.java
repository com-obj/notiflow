package com.obj.nc.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.List;

@Data
public class SendSmsRequestDto {

    @NotEmpty
    private List<String> address;

    @NotBlank
    private String clientCorrelator;

    @NotBlank
    private String message;

    @NotBlank
    private String notifyURL;

    @NotBlank
    private String senderAddress;

    @NotBlank
    private String billCode;

}
