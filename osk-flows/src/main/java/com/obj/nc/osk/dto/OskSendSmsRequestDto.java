package com.obj.nc.osk.dto;

import com.obj.nc.services.Sms;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Data
@EqualsAndHashCode(callSuper = true)
public class OskSendSmsRequestDto extends Sms {

    @NotBlank
    private String clientCorrelator;

    @NotNull
    private String notifyURL;

    @NotBlank
    private String billCode;

    public void setClientCorrelator(String clientCorrelatorPrefix) {
        this.clientCorrelator = clientCorrelatorPrefix + "-" + createTimestamp();
    }

    public static String createTimestamp() {
        ZonedDateTime zdt = ZonedDateTime.now();
        // example format: 2014-09-02T08:05:23.653Z
        return DateTimeFormatter.ISO_INSTANT.format(zdt);
    }

}

