package com.obj.nc.functions.processors.messageTemplating;

import java.util.Locale;
import java.util.Optional;

import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;

import com.obj.nc.domain.content.Content;
import com.obj.nc.domain.content.TemplateWithModelContent;
import com.obj.nc.domain.content.sms.SimpleTextContent;
import com.obj.nc.domain.message.Message;
import com.obj.nc.exceptions.PayloadValidationException;

@Component
public class SmsTemplateFormatter extends BaseTemplateFormatter {

	public SmsTemplateFormatter(TemplateEngine templateEngine, ThymeleafConfiguration config) {
		super(templateEngine, config);
	}

	@Override
	public Optional<PayloadValidationException> checkPreCondition(Message message) {
		Content content = message.getBody().getMessage();
		
		if (!(content instanceof  TemplateWithModelContent)) {
			return Optional.of(new PayloadValidationException("SmsTemplateFormatter cannot format message because its content is not of type TemplateWithJsonModelSmsContent. Instead is " +  content.getClass().getSimpleName()));
		}		

		return Optional.empty();
	}

	protected Message createMessageWithFormattedContent(String formatedContent, Locale locale,  Message payload) {		
		Message smsMessage = Message.createAsSms();

		SimpleTextContent smsContent = smsMessage.getContentTyped();
		smsContent.setText(formatedContent);
		
		return smsMessage;
	}

}
