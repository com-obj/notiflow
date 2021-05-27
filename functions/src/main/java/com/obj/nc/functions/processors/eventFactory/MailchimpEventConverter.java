package com.obj.nc.functions.processors.eventFactory;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.obj.nc.aspects.DocumentProcessingInfo;
import com.obj.nc.components.api.MailchimpContentFactory;
import com.obj.nc.domain.content.mailchimp.MailchimpContent;
import com.obj.nc.domain.content.mailchimp.MailchimpData;
import com.obj.nc.domain.event.GenericEvent;
import com.obj.nc.domain.message.MailChimpMessage;
import com.obj.nc.exceptions.PayloadValidationException;
import com.obj.nc.functions.processors.ProcessorFunctionAdapter;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@DocumentProcessingInfo("MailchimpEventConverter")
public class MailchimpEventConverter extends ProcessorFunctionAdapter<GenericEvent, MailChimpMessage> {
	
	private final MailchimpContentFactory mailchimpContentFactoryImpl;
	
	@Override
	protected Optional<PayloadValidationException> checkPreCondition(GenericEvent payload) {
		if (payload == null) {
			return Optional.of(new PayloadValidationException("GenericEvent must not be null"));
		} else if (payload.getPayloadJson()==null) {
			return Optional.of(new PayloadValidationException("GenericEvent doesn't contain original message"));
		} else if (!(payload.getPayloadAsPojo() instanceof MailchimpData)) {
			return Optional.of(new PayloadValidationException(String.format("Payload is not an instance of class %s. Is %s",
					MailchimpData.class.getSimpleName(), payload.getPayloadAsPojo().getClass().getSimpleName())));
		} else if (payload.<MailchimpData>getPayloadAsPojo().getSubject() == null) {
			return Optional.of(new PayloadValidationException("Subject of mailchimp event must not be null"));
		} else {
			return Optional.empty();
		}
	}
	
	@Override
	protected MailChimpMessage execute(GenericEvent payload) {
		MailChimpMessage message = new MailChimpMessage();
		message.getHeader().setFlowId(payload.getFlowId());
		
		MailchimpData mailchimpData = payload.getPayloadAsPojo();
		MailchimpContent content = mailchimpContentFactoryImpl.createFromData(mailchimpData);
		
		message.setBody(content);
		return message;
	}
	
}
