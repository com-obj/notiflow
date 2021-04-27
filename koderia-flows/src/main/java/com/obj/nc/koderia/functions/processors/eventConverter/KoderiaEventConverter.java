package com.obj.nc.koderia.functions.processors.eventConverter;

import com.obj.nc.aspects.DocumentProcessingInfo;
import com.obj.nc.domain.event.GenericEvent;
import com.obj.nc.domain.notifIntent.NotificationIntent;
import com.obj.nc.exceptions.PayloadValidationException;
import com.obj.nc.functions.processors.ProcessorFunctionAdapter;

import com.obj.nc.koderia.domain.event.BaseKoderiaEvent;
import com.obj.nc.mappers.MailchimpDataToMailchimpContentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
@DocumentProcessingInfo("KoderiaEventConverter")
public class KoderiaEventConverter extends ProcessorFunctionAdapter<GenericEvent, NotificationIntent> {
	
	private final MailchimpDataToMailchimpContentMapper mailchimpDataToMailchimpContentMapper;
	
	@Override
	protected Optional<PayloadValidationException> checkPreCondition(GenericEvent payload) {
		if (payload == null) {
			return Optional.of(new PayloadValidationException("GenericEvent must not be null"));
		} else if (payload.getPayloadJson()==null) {
			return Optional.of(new PayloadValidationException("GenericEvent doesn't contain original message"));
		} else if (payload.<BaseKoderiaEvent>getPayloadAsPojo().getMessageSubject() == null) {
			return Optional.of(new PayloadValidationException("Subject of Koderia event must not be null"));
		} else if (payload.<BaseKoderiaEvent>getPayloadAsPojo().getMessageText() == null) {
			return Optional.of(new PayloadValidationException("Text of Koderia event must not be null"));
		} else {
			return Optional.empty();
		}
	}
	
	@Override
	protected NotificationIntent execute(GenericEvent payload) {
		NotificationIntent notificationIntent = new NotificationIntent();
		notificationIntent.getHeader().setFlowId(payload.getFlowId());
		
		BaseKoderiaEvent koderiaEvent = payload.getPayloadAsPojo();
		notificationIntent.getBody().setMessage(mailchimpDataToMailchimpContentMapper.map(koderiaEvent));
		return notificationIntent;
	}
	
}
