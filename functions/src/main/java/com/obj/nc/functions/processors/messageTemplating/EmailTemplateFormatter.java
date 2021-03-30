package com.obj.nc.functions.processors.messageTemplating;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.obj.nc.aspects.DocumentProcessingInfo;
import com.obj.nc.domain.BaseJSONObject;
import com.obj.nc.domain.content.Content;
import com.obj.nc.domain.content.EmailContent;
import com.obj.nc.domain.content.TemplateWithModelBasedContent;
import com.obj.nc.domain.message.Message;
import com.obj.nc.exceptions.PayloadValidationException;
import com.obj.nc.functions.processors.ProcessorFunctionAdapter;

import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
public class EmailTemplateFormatter extends ProcessorFunctionAdapter<Message, List<Message>> {
	
	public final static String LOCALE_ATTR_NAME = "@locale";
	
	private TemplateEngine templateEngine;
	
	private ThymeleafConfiguration config;

	@Override
	public Optional<PayloadValidationException> checkPreCondition(Message message) {
		Content content = message.getBody().getMessage();

		if (content ==null ) {
			return Optional.of(new PayloadValidationException("EmailTemplateFormatter cannot format message because its content is null"));
		}
		
		if (!(content instanceof  TemplateWithModelBasedContent)) {
			return Optional.of(new PayloadValidationException("EmailTemplateFormatter cannot format message because its content is not of type EmailFromTemplateJsonModel. Instead is " +  content.getClass().getSimpleName()));
		}		

		return Optional.empty();
	}


	@DocumentProcessingInfo("EmailTemplateFormatter")
	@Override
	public List<Message> execute(Message payload) {
		List<Message> result = new ArrayList<>();
		
		TemplateWithModelBasedContent<?> emailFromTemplate = payload.getContentTyped();
		
		List<Locale> forLocales = CollectionUtils.isEmpty(emailFromTemplate.getRequiredLocales())?
				config.getDefaultLocales()
				:
				emailFromTemplate.getRequiredLocales();
		
		for (Locale locale: forLocales) {
			Context ctx = new Context(locale);
			if (emailFromTemplate.getModel() instanceof BaseJSONObject) {
				ctx.setVariables(((BaseJSONObject)emailFromTemplate.getModel()).getAttributes());
			} else {
				ctx.setVariable("model", emailFromTemplate.getModel());
			}
			
			
			final String htmlContent = this.templateEngine.process(emailFromTemplate.getTemplateFileName(), ctx);
			
			Message htmlMessage = Message.createAsEmail();
			htmlMessage.setAttributeValue(LOCALE_ATTR_NAME,locale);
			htmlMessage.getBody().setDeliveryOptions(payload.getBody().getDeliveryOptions());
			htmlMessage.getBody().setRecievingEndpoints(payload.getBody().getRecievingEndpoints());
			
			EmailContent emailContent = htmlMessage.getContentTyped();
			emailContent.setContentType(MediaType.TEXT_HTML_VALUE);
			emailContent.setSubject(emailFromTemplate.getSubjectLocalised(locale));
			emailContent.setText(htmlContent);
			
			result.add(htmlMessage);
		}
		
		return result;
	}


}
