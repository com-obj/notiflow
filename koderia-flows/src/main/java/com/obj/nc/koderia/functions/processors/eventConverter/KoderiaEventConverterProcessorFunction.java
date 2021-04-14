package com.obj.nc.koderia.functions.processors.eventConverter;

import com.obj.nc.aspects.DocumentProcessingInfo;
import com.obj.nc.domain.content.email.EmailContent;
import com.obj.nc.domain.notifIntent.NotificationIntent;
import com.obj.nc.exceptions.PayloadValidationException;
import com.obj.nc.functions.processors.ProcessorFunctionAdapter;
import com.obj.nc.koderia.dto.EmitEventDto;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@AllArgsConstructor
@DocumentProcessingInfo("KoderiaEventConverter")
public class KoderiaEventConverterProcessorFunction extends ProcessorFunctionAdapter<EmitEventDto, NotificationIntent> {
	public static final String ORIGINAL_EVENT_FIELD = "originalEvent";
	
	@Override
	protected Optional<PayloadValidationException> checkPreCondition(EmitEventDto payload) {
		if (payload == null) {
			return Optional.of(new PayloadValidationException("Koderia event must not be null"));
		}
		
		if (payload.getData().getMessageSubject() == null) {
			return Optional.of(new PayloadValidationException("Subject of Koderia event must not be null"));
		}
		
		if (payload.getData().getMessageText() == null) {
			return Optional.of(new PayloadValidationException("Text of Koderia event must not be null"));
		}
		
		return Optional.empty();
	}
	
	@Override
	protected NotificationIntent execute(EmitEventDto payload) {
		NotificationIntent notificationIntent = new NotificationIntent();
		notificationIntent.getHeader().setFlowId("static-routing-pipeline");
		notificationIntent.getBody().setMessage(
				EmailContent.createWithSubject(payload.getData().getMessageSubject(), payload.getData().getMessageText())
		);
		notificationIntent.getBody().getMessage().setAttributeValue(ORIGINAL_EVENT_FIELD, payload.asMap());
		return notificationIntent;
	}
}
