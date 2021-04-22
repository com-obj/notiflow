package com.obj.nc.functions.processors.messageTemplating;

import java.util.Locale;
import java.util.Optional;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;

import com.obj.nc.aspects.DocumentProcessingInfo;
import com.obj.nc.domain.content.Content;
import com.obj.nc.domain.content.email.EmailContent;
import com.obj.nc.domain.content.email.TemplateWithModelEmailContent;
import com.obj.nc.domain.message.Message;
import com.obj.nc.exceptions.PayloadValidationException;
import com.obj.nc.functions.processors.messageTemplating.config.ThymeleafConfigProperties;
import com.obj.nc.functions.processors.messageTemplating.config.ThymeleafConfiguration;

@Component
@DocumentProcessingInfo("EmailFormatter")
public class EmailTemplateFormatter extends BaseTemplateFormatter {

	public EmailTemplateFormatter(TemplateEngine templateEngine, ThymeleafConfiguration config, ThymeleafConfigProperties props) {
		super(templateEngine, config, props);
	}

	@Override
	public Optional<PayloadValidationException> checkPreCondition(Message message) {
		Content content = message.getBody().getMessage();
		
		if (!(content instanceof  TemplateWithModelEmailContent)) {
			return Optional.of(new PayloadValidationException("EmailTemplateFormatter cannot format message because its content is not of type TemplateWithModelEmailContent. Instead is " +  content.getClass().getSimpleName()));
		}		

		return Optional.empty();
	}

	protected Message createMessageWithFormattedContent(String formatedContent, Locale locale,  Message payload) {		
		Message htmlMessage = Message.createAsEmail();

		EmailContent emailContent = htmlMessage.getContentTyped();
		emailContent.setContentType(MediaType.TEXT_HTML_VALUE);
		
		TemplateWithModelEmailContent<?> emailFromTemplate = payload.getContentTyped();
		emailContent.setSubject(emailFromTemplate.getSubjectLocalised(locale));
		emailContent.setText(formatedContent);
		
		return htmlMessage;
	}

}
