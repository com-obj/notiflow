package com.obj.nc.functions.processors.senders;

import com.obj.nc.aspects.DocumentProcessingInfo;
import com.obj.nc.domain.message.Message;
import com.obj.nc.dto.mailchimp.MessageResponseDto;
import com.obj.nc.dto.mailchimp.SendMessageWithTemplateDto;
import com.obj.nc.mapper.MailchimpMessageMapper;
import com.obj.nc.mapper.MailchimpMessageMapperAggregateImpl;
import com.obj.nc.mapper.MailchimpMessageMapperImpl;
import com.obj.nc.services.MailchimpService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Function;

@Component
@Log4j2
public class MailchimpSenderExecution implements Function<Message, Message> {

	public static final String MAILCHIMP_RESPONSE_FIELD = "mailchimpResponse";

	@Autowired
	private MailchimpService mailchimpService;

	@Autowired
	@Qualifier(MailchimpMessageMapperImpl.COMPONENT_NAME)
	private MailchimpMessageMapper mailchimpMessageMapper;

	@Autowired
	@Qualifier(MailchimpMessageMapperAggregateImpl.COMPONENT_NAME)
	private MailchimpMessageMapper mailchimpAggregateMessageMapper;

	@DocumentProcessingInfo("SendMailchimpMessage")
	@Override
	public Message apply(Message message) {
		SendMessageWithTemplateDto sendMessageDto = message.isAggregateMessage()
				? mailchimpAggregateMessageMapper.mapWithTemplate(message)
				: mailchimpMessageMapper.mapWithTemplate(message);

		List<MessageResponseDto> messageResponseDtos = mailchimpService.sendMessageWithTemplate(sendMessageDto);
		message.getBody().putAttributeValue(MAILCHIMP_RESPONSE_FIELD, messageResponseDtos);

		return message;
	}

}