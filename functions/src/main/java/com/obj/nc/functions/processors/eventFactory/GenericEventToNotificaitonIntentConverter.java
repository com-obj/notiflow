package com.obj.nc.functions.processors.eventFactory;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.obj.nc.domain.event.GenericEvent;
import com.obj.nc.domain.notifIntent.NotificationIntent;
import com.obj.nc.exceptions.PayloadValidationException;
import com.obj.nc.functions.processors.ProcessorFunctionAdapter;

import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
public class GenericEventToNotificaitonIntentConverter extends ProcessorFunctionAdapter<GenericEvent, NotificationIntent<?>> {

	@Override
	protected Optional<PayloadValidationException> checkPreCondition(GenericEvent payload) {
		if (payload == null) {
			return Optional.of(new PayloadValidationException("Generic event must not be null"));
		}

		return Optional.empty();
	}

	@Override
	protected NotificationIntent<?> execute(GenericEvent genericEvent) {
		NotificationIntent<?> notificationIntent = new NotificationIntent<Object>();

		notificationIntent.setBody(genericEvent.getPayloadAsPojo());
		
		return notificationIntent;
	}


}
