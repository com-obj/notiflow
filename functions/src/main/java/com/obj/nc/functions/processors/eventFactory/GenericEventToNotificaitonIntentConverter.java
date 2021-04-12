package com.obj.nc.functions.processors.eventFactory;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.obj.nc.domain.event.GenericEvent;
import com.obj.nc.domain.notifIntent.NotificationIntent;
import com.obj.nc.exceptions.PayloadValidationException;
import com.obj.nc.functions.processors.ProcessorFunctionAdapter;
import com.obj.nc.utils.JsonUtils;

import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
public class GenericEventToNotificaitonIntentConverter extends ProcessorFunctionAdapter<GenericEvent, NotificationIntent> {

	@Override
	protected Optional<PayloadValidationException> checkPreCondition(GenericEvent payload) {
		if (payload == null) {
			return Optional.of(new PayloadValidationException("Generic event must not be null"));
		}

		return Optional.empty();
	}

	@Override
	protected NotificationIntent execute(GenericEvent genericEvent) {
		NotificationIntent notificationIntent = new NotificationIntent();
		
		notificationIntent.getHeader().setFlowId(genericEvent.getFlowId());
		notificationIntent.getHeader().generateAndSetID();
		notificationIntent.getHeader().addEventId(notificationIntent.getHeader().getId());

		notificationIntent.getBody().setAttributeValue(ORIGINAL_EVENT_FIELD, JsonUtils.writeObjectToJSONString(genericEvent));
		
		return notificationIntent;
	}
	
	public static final String ORIGINAL_EVENT_FIELD = "originalEvent";


}
