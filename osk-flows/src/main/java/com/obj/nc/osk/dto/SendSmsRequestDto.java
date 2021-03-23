package com.obj.nc.osk.dto;

import com.obj.nc.domain.endpoints.RecievingEndpoint;
import com.obj.nc.domain.message.Message;
import com.obj.nc.domain.message.SimpleText;
import com.obj.nc.osk.functions.senders.SmsSenderConfigProperties;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class SendSmsRequestDto {

    @NotNull
    private List<String> address;

    @NotBlank
    private String clientCorrelator;

    @NotNull
    private String message;

    @NotNull
    private String notifyURL;

    @NotBlank
    private String senderAddress;

    @NotBlank
    private String billCode;

    public static SendSmsRequestDto from(Message payload, SmsSenderConfigProperties properties) {
        SendSmsRequestDto result = new SendSmsRequestDto();

        result.address = payload.getBody().getRecievingEndpoints().stream()
                .map(RecievingEndpoint::getEndpointId)
                .collect(Collectors.toList());

        result.clientCorrelator = properties.getClientCorrelator() + "-" + createTimestamp();

        SimpleText content = payload.getBody().getContentTyped();
        result.message = content.getText();

        result.notifyURL = properties.getNotifyUrl();
        result.senderAddress = properties.getSenderAddress();
        result.billCode = properties.getBillCode();

        return result;
    }

    private static String createTimestamp() {
        ZonedDateTime zdt = ZonedDateTime.now();
        // example format: 2014-09-02T08:05:23.653Z
        return DateTimeFormatter.ISO_INSTANT.format(zdt);
    }

}
