package com.obj.nc.functions.processors.senders;

import com.obj.nc.domain.message.Message;

import java.util.function.Function;

public interface MailchimpSender extends Function<Message, Message> {

}
