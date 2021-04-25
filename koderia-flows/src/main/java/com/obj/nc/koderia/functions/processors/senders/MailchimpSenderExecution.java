package com.obj.nc.koderia.functions.processors.senders;

import java.util.List;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.obj.nc.aspects.DocumentProcessingInfo;
import com.obj.nc.domain.message.Message;
import com.obj.nc.koderia.dto.mailchimp.MessageResponseDto;
import com.obj.nc.koderia.dto.mailchimp.SendMessageWithTemplateDto;
import com.obj.nc.koderia.mapper.MailchimpMessageMapper;
import com.obj.nc.koderia.mapper.MailchimpMessageMapperImpl;
import com.obj.nc.koderia.services.MailchimpClient;

import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
@DocumentProcessingInfo("SendMailchimpMessage")
public class MailchimpSenderExecution implements Function<Message, Message> {

	public static final String MAILCHIMP_RESPONSE_FIELD = "mailchimpResponse";

	@Autowired
	private MailchimpClient mailchimpClient;

	@Autowired
	@Qualifier(MailchimpMessageMapperImpl.COMPONENT_NAME)
	private MailchimpMessageMapper mailchimpMessageMapper;

//	@Autowired
//	@Qualifier(MailchimpMessageMapperAggregateImpl.COMPONENT_NAME)
//	private MailchimpMessageMapper mailchimpAggregateMessageMapper;


	@Override
	public Message apply(Message message) {
		SendMessageWithTemplateDto sendMessageDto = mailchimpMessageMapper.mapWithTemplate(message);
//				? mailchimpAggregateMessageMapper.mapWithTemplate(message)
//				: mailchimpMessageMapper.mapWithTemplate(message);

		List<MessageResponseDto> messageResponseDtos = mailchimpClient.sendMessageWithTemplate(sendMessageDto);
		message.getBody().setAttributeValue(MAILCHIMP_RESPONSE_FIELD, messageResponseDtos);

		return message;
	}

}