package com.obj.nc.functions.processors.eventFactory;

import com.obj.nc.aspects.DocumentProcessingInfo;
import com.obj.nc.domain.content.mailchimp.MailchimpData;
import com.obj.nc.domain.event.GenericEvent;
import com.obj.nc.domain.notifIntent.NotificationIntent;
import com.obj.nc.exceptions.PayloadValidationException;
import com.obj.nc.functions.processors.ProcessorFunctionAdapter;

import com.obj.nc.mappers.MailchimpContentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
@DocumentProcessingInfo("MailchimpEventConverter")
public class MailchimpEventConverter extends ProcessorFunctionAdapter<GenericEvent, NotificationIntent> {
	
	private final MailchimpContentMapper mailchimpContentMapper;
	
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
	protected NotificationIntent execute(GenericEvent payload) {
		NotificationIntent notificationIntent = new NotificationIntent();
		notificationIntent.getHeader().setFlowId(payload.getFlowId());
		
		MailchimpData mailchimpData = payload.getPayloadAsPojo();
		notificationIntent.getBody().setMessage(mailchimpContentMapper.map(mailchimpData));
		return notificationIntent;
	}
	
}
