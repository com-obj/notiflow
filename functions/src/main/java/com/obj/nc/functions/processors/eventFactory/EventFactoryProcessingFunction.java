package com.obj.nc.functions.processors.eventFactory;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.obj.nc.domain.event.Event;
import com.obj.nc.domain.event.GenericEvent;
import com.obj.nc.exceptions.PayloadValidationException;
import com.obj.nc.functions.processors.ProcessorFunctionAdapter;
import com.obj.nc.utils.JsonUtils;

import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
public class EventFactoryProcessingFunction extends ProcessorFunctionAdapter<GenericEvent, Event> {

	@Override
	protected Optional<PayloadValidationException> checkPreCondition(GenericEvent payload) {
		if (payload == null) {
			return Optional.of(new PayloadValidationException("Generic event must not be null"));
		}

		return Optional.empty();
	}

	@Override
	protected Event execute(GenericEvent genericEvent) {
		Event event = new Event();
		event.stepStart("EventFactory");
		
		event.getHeader().setFlowId(genericEvent.getFlowId());
		event.getHeader().generateAndSetID();
		event.getHeader().addEventId(event.getHeader().getId());

		event.getBody().putAttributeValue(ORIGINAL_EVENT_FIELD, JsonUtils.writeObjectToJSONString(genericEvent));
		
		event.stepFinish();
		
		return event;
	}
	
	public static final String ORIGINAL_EVENT_FIELD = "originalEvent";


}
