package com.obj.nc.osk.functions.processors.sms.dtos;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@NoArgsConstructor
public class OskSendSmsRequestDto {
    
    @NotNull
    private String senderAddress;

    @NotNull
    private List<String> address;

    @NotNull
    private String message;

    @NotBlank
    private String clientCorrelator;

    @NotNull
    private String notifyURL;

    @NotBlank
    private String billCode;

}

