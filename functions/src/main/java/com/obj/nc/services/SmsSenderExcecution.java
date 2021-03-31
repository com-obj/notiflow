package com.obj.nc.services;

import com.obj.nc.domain.message.Message;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public interface SmsClient<REQUEST_T, RESPONSE_T> {

    REQUEST_T convertMessageToRequest(Message message);

    RESPONSE_T sendRequest(@Valid @NotNull REQUEST_T smsMessage);

}
