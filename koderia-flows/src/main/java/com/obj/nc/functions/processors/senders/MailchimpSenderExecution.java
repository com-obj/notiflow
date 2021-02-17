package com.obj.nc.functions.processors.senders;

import com.obj.nc.domain.message.Message;
import com.obj.nc.dto.mailchimp.MessageResponseDto;
import com.obj.nc.dto.mailchimp.SendMessageWithTemplateDto;
import com.obj.nc.mapper.MailchimpMessageMapper;
import com.obj.nc.services.MailchimpService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Function;

@Component
@Log4j2
public class MailchimpSenderExecution implements Function<Message, Message> {

	@Autowired
	private MailchimpService mailchimpService;

	@Autowired
	private MailchimpMessageMapper mailchimpMessageMapper;

	@Override
	public Message apply(Message message) {
		SendMessageWithTemplateDto sendMessageDto = mailchimpMessageMapper.mapWithTemplate(message);
		List<MessageResponseDto> messageResponseDtos = mailchimpService.sendMessageWithTemplate(sendMessageDto);
		return message;
	}

}