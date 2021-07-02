package com.obj.nc.functions.processors.senders;

import java.util.function.Function;

import com.obj.nc.domain.message.SmsMessage;

public interface SmsSender extends Function<SmsMessage, SmsMessage>{

}
