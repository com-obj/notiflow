package com.obj.nc.koderia.functions.processors.messageExtractor;

import com.obj.nc.aspects.DocumentProcessingInfo;
import com.obj.nc.domain.content.email.EmailContent;
import com.obj.nc.domain.notifIntent.NotificationIntent;
import com.obj.nc.exceptions.PayloadValidationException;
import com.obj.nc.functions.processors.ProcessorFunctionAdapter;

import com.obj.nc.koderia.dto.koderia.event.BaseKoderiaEventDto;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static com.obj.nc.functions.processors.eventFactory.GenericEventToNotificaitonIntentConverter.ORIGINAL_EVENT_FIELD;

@Component
@AllArgsConstructor
@DocumentProcessingInfo("KoderiaMessageExtractor")
public class KoderiaMessageExtractorProcessorFunction extends ProcessorFunctionAdapter<NotificationIntent, NotificationIntent> {
	@Override
	protected Optional<PayloadValidationException> checkPreCondition(NotificationIntent payload) {
		if (payload == null) {
			return Optional.of(new PayloadValidationException("Koderia event must not be null"));
		}
		
		BaseKoderiaEventDto originalEvent = payload.getBody().getAttributeValueAs(ORIGINAL_EVENT_FIELD, BaseKoderiaEventDto.class);
		if (originalEvent.getMessageSubject() == null) {
			return Optional.of(new PayloadValidationException("Subject of Koderia event must not be null"));
		}
		if (originalEvent.getMessageText() == null) {
			return Optional.of(new PayloadValidationException("Text of Koderia event must not be null"));
		}
		
		return Optional.empty();
	}
	
	@Override
	protected NotificationIntent execute(NotificationIntent payload) {
		BaseKoderiaEventDto originalEvent = payload.getBody().getAttributeValueAs(ORIGINAL_EVENT_FIELD, BaseKoderiaEventDto.class);
		payload.getBody().setMessage(
				EmailContent.createWithSubject(originalEvent.getMessageSubject(), originalEvent.getMessageText())
		);
		payload.getBody().getMessage().setAttributeValue(ORIGINAL_EVENT_FIELD, originalEvent);
		payload.getBody().removeAttribute(ORIGINAL_EVENT_FIELD);
		return payload;
	}
}
