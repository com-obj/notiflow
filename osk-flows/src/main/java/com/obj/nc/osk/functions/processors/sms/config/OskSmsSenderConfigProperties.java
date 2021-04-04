package com.obj.nc.osk.functions.processors.sms.config;

import javax.validation.constraints.NotBlank;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import lombok.Data;

@Data
@Validated
@Configuration
@ConfigurationProperties(prefix = "osk.sia.functions.sms-sender")
public class OskSmsSenderConfigProperties {

    @NotBlank
    private String gapApiUrl;

    @NotBlank
    private String gapApiLogin;

    @NotBlank
    private String gapApiPassword;

    @NotBlank
    private String clientCorrelatorPrefix;

    private String notifyUrl;

    @NotBlank
    private String senderAddress;

    @NotBlank
    private String billCode;

}
