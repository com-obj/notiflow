package com.obj.nc.functions.processors.messageTemplating;

import java.util.Locale;
import java.util.Optional;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;

import com.obj.nc.aspects.DocumentProcessingInfo;
import com.obj.nc.components.api.MessageFactory;
import com.obj.nc.domain.content.Content;
import com.obj.nc.domain.content.email.EmailContent;
import com.obj.nc.domain.content.email.TemplateWithModelEmailContent;
import com.obj.nc.domain.message.EmailMessage;
import com.obj.nc.domain.message.Message;
import com.obj.nc.exceptions.PayloadValidationException;
import com.obj.nc.functions.processors.messageTemplating.config.ThymeleafConfiguration;

@Component
@DocumentProcessingInfo("EmailFormatter")
public class EmailTemplateFormatter extends BaseTemplateFormatter<TemplateWithModelEmailContent<?>, EmailContent> {

	private final MessageFactory messageFactory;
	
	public EmailTemplateFormatter(TemplateEngine templateEngine, ThymeleafConfiguration config, MessageFactory messageFactory) {
		super(templateEngine, config);
		
		this.messageFactory = messageFactory;
	}
	
	@Override
	public Optional<PayloadValidationException> checkPreCondition(Message<TemplateWithModelEmailContent<?>> message) {
		Content content = message.getBody();
		
		if (!(content instanceof  TemplateWithModelEmailContent)) {
			return Optional.of(new PayloadValidationException("EmailTemplateFormatter cannot format message because its content is not of type TemplateWithModelEmailContent. Instead is " +  content.getClass().getSimpleName()));
		}		

		return Optional.empty();
	}

	protected EmailMessage createMessageWithFormattedContent(String formatedContent, Locale locale,  Message<TemplateWithModelEmailContent<?>> payload) {		
		EmailMessage htmlMessage = messageFactory.createAsEmail();

		EmailContent emailContent = htmlMessage.getBody();
		emailContent.setContentType(MediaType.TEXT_HTML_VALUE);
		
		TemplateWithModelEmailContent<?> emailFromTemplate = payload.getBody();
		emailContent.setSubject(emailFromTemplate.getSubjectLocalised(locale));
		emailContent.setText(formatedContent);
		
		return htmlMessage;
	}

}
