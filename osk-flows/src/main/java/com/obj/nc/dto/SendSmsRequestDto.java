package com.obj.nc.dto;

import lombok.Data;

import java.util.List;

@Data
public class SendSmsRequestDto {

    private List<String> address;

    private String clientCorrelator;

    private String message;

    private String notifyURL;

    private String senderAddress;

    private String billCode;

}
