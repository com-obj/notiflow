package com.obj.nc.services;

import com.obj.nc.domain.message.Message;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public interface SmsClient<SMS_T extends Sms, RESPONSE_T> {

    SMS_T convertMessage(Message message);

    RESPONSE_T send(@Valid @NotNull SMS_T smsMessage);

}
