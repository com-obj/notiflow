package com.obj.nc.osk.functions.senders;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

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

    @NotNull
    private String notifyUrl;

    @NotBlank
    private String senderAddress;

    @NotBlank
    private String billCode;

}
