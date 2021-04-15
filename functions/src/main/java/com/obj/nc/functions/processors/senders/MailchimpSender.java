package com.obj.nc.functions.processors.senders;

import com.obj.nc.domain.message.Message;
import com.obj.nc.flows.testmode.mailchimp.dto.MessageResponseDto;
import com.obj.nc.flows.testmode.mailchimp.dto.SendMessageWithTemplateDto;

import java.util.List;
import java.util.function.Function;

public interface MailchimpSender extends Function<Message, Message> {

    List<MessageResponseDto> sendMessageWithTemplate(SendMessageWithTemplateDto sendMessageDto);

}
