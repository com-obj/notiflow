package com.obj.nc.functions.processors.messageTemplating;

import java.util.Locale;
import java.util.Optional;

import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;

import com.obj.nc.aspects.DocumentProcessingInfo;
import com.obj.nc.components.api.MessageFactory;
import com.obj.nc.domain.content.Content;
import com.obj.nc.domain.content.TemplateWithModelContent;
import com.obj.nc.domain.content.sms.SimpleTextContent;
import com.obj.nc.domain.message.Message;
import com.obj.nc.exceptions.PayloadValidationException;
import com.obj.nc.functions.processors.messageTemplating.config.ThymeleafConfiguration;

@Component
@DocumentProcessingInfo("SmsFormatter")
public class SmsTemplateFormatter extends BaseTemplateFormatter<TemplateWithModelContent<?>, SimpleTextContent> {

	private final MessageFactory messageFactory;
	
	public SmsTemplateFormatter(TemplateEngine templateEngine, ThymeleafConfiguration config, MessageFactory messageFactory) {
		super(templateEngine, config);
		
		this.messageFactory = messageFactory;
	}

	@Override
	public Optional<PayloadValidationException> checkPreCondition(Message<TemplateWithModelContent<?>> message) {
		Content content = message.getBody();
		
		if (!(content instanceof  TemplateWithModelContent)) {
			return Optional.of(new PayloadValidationException("SmsTemplateFormatter cannot format message because its content is not of type TemplateWithJsonModelSmsContent. Instead is " +  content.getClass().getSimpleName()));
		}		

		return Optional.empty();
	}

	protected Message<SimpleTextContent> createMessageWithFormattedContent(String formatedContent, Locale locale,  Message<TemplateWithModelContent<?>> payload) {		
		Message<SimpleTextContent> smsMessage = messageFactory.createAsSms();

		SimpleTextContent smsContent = smsMessage.getBody();
		smsContent.setText(formatedContent);
		
		return smsMessage;
	}

}
