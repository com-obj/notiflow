package com.obj.nc.functions.processors.senders;

import com.obj.nc.domain.message.Message;
import org.springframework.web.client.AsyncRestTemplate;
import org.springframework.web.client.RestTemplate;

import java.util.function.Function;

public interface MailchimpSender extends Function<Message, Message> {
    
}
