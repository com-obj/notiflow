package com.obj.nc.osk.service;

public interface SmsRestClientConstants {

    String SEND_PATH = "/outbound/{senderAddress}/requests";

    String STATUS_SUCCESS = "SUCCESS";
    String STATUS_FAILURE = "FAILURE";

    String SEND_SMS_REQUEST_ATTRIBUTE = "sendSmsRequest";
    String SEND_SMS_RESPONSE_ATTRIBUTE = "sendSmsResponse";

}
