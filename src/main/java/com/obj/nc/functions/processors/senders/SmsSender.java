package com.obj.nc.functions.processors.senders;

import java.util.function.Function;

import com.obj.nc.domain.message.SmstMessage;

public interface SmsSender extends Function<SmstMessage, SmstMessage>{

}
