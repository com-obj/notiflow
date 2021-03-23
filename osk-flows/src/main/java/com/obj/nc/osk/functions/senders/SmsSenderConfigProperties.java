package com.obj.nc.osk.functions.senders;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@Configuration
@ConfigurationProperties(prefix = "osk.sia.functions.sms-sender")
public class SmsSenderConfigProperties {

    @NotBlank
    private String gapApiUrl;

    @NotBlank
    private String gapApiLogin;

    @NotBlank
    private String gapApiPassword;

    @NotBlank
    private String clientCorrelatorPrefix;

    @NotNull
    private String notifyUrl;

    @NotBlank
    private String senderAddress;

    @NotBlank
    private String billCode;

}
