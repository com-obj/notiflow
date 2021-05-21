package com.obj.nc.functions.processors.senders;

import java.util.function.Function;

import com.obj.nc.domain.content.mailchimp.MailchimpContent;
import com.obj.nc.domain.message.Message;

public interface MailchimpSender extends Function<Message<MailchimpContent>, Message<MailchimpContent>> {
    
}
