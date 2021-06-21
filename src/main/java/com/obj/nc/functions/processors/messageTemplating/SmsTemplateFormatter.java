package com.obj.nc.functions.processors.messageTemplating;

import java.util.Locale;
import java.util.Optional;

import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;

import com.obj.nc.aspects.DocumentProcessingInfo;
import com.obj.nc.domain.content.MessageContent;
import com.obj.nc.domain.content.TemplateWithModelContent;
import com.obj.nc.domain.content.sms.SimpleTextContent;
import com.obj.nc.domain.message.Message;
import com.obj.nc.domain.message.SmstMessage;
import com.obj.nc.exceptions.PayloadValidationException;
import com.obj.nc.functions.processors.messageTemplating.config.ThymeleafConfiguration;

@Component
@DocumentProcessingInfo("SmsFormatter")
public class SmsTemplateFormatter extends BaseTemplateFormatter<TemplateWithModelContent<?>, SimpleTextContent> {

	public SmsTemplateFormatter(TemplateEngine templateEngine, ThymeleafConfiguration config) {
		super(templateEngine, config);
	}

	@Override
	public Optional<PayloadValidationException> checkPreCondition(Message<TemplateWithModelContent<?>> message) {
		MessageContent content = message.getBody();
		
		if (!(content instanceof  TemplateWithModelContent)) {
			return Optional.of(new PayloadValidationException("SmsTemplateFormatter cannot format message because its content is not of type TemplateWithJsonModelSmsContent. Instead is " +  content.getClass().getSimpleName()));
		}		

		return Optional.empty();
	}

	protected Message<SimpleTextContent> createMessageWithFormattedContent(String formatedContent, Locale locale,  Message<TemplateWithModelContent<?>> payload) {		
		SmstMessage smsMessage = new SmstMessage();

		smsMessage.getBody().setText(formatedContent);
		
		return smsMessage;
	}

}
