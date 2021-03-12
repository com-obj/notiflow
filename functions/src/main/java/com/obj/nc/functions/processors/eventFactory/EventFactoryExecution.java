package com.obj.nc.functions.processors.eventFactory;

import java.util.function.Function;

import org.springframework.stereotype.Component;

import com.obj.nc.aspects.DocumentProcessingInfo;
import com.obj.nc.domain.event.Event;
import com.obj.nc.domain.event.GenericEvent;
import com.obj.nc.utils.JsonUtils;

import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
public class EventFactoryExecution implements Function<GenericEvent, Event> {

	public static final String ORIGINAL_EVENT_FIELD = "originalEvent";

	@Override
	public Event apply(GenericEvent recievedGenericEvent) {
		Event event = new Event();
		event.stepStart("EventFactory");
		
		event.getHeader().setFlowId(recievedGenericEvent.getFlowId());
		event.getHeader().generateAndSetID();
		event.getHeader().addEventId(event.getHeader().getId());

		event.getBody().putAttributeValue(ORIGINAL_EVENT_FIELD, JsonUtils.writeObjectToJSONString(recievedGenericEvent));
		
		event.stepFinish();
		
		return event;
	}

}