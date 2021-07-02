package com.obj.nc.functions.processors.senders;

import java.util.function.Function;

import com.obj.nc.domain.message.MailChimpMessage;

public interface MailchimpSender extends Function<MailChimpMessage, MailChimpMessage> {
    
}
