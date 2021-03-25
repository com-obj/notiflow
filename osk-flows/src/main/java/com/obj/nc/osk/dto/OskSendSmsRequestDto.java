package com.obj.nc.osk.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
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

    public void setClientCorrelator(String clientCorrelatorPrefix) {
        this.clientCorrelator = clientCorrelatorPrefix + "-" + createTimestamp();
    }
    
    private String createTimestamp() {
        ZonedDateTime zdt = ZonedDateTime.now();
        // example format: 2014-09-02T08:05:23.653Z
        return DateTimeFormatter.ISO_INSTANT.format(zdt);
    }
    
}

