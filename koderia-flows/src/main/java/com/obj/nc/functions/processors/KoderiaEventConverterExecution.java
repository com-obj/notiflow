package com.obj.nc.functions.processors;

import com.obj.nc.domain.event.Event;
import com.obj.nc.dto.EmitEventDto;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.function.Function;

@Component
@Log4j2
public class KoderiaEventConverterExecution implements Function<EmitEventDto, Event> {

	public static final String ORIGINAL_EVENT_FIELD = "originalEvent";

	@Override
	public Event apply(EmitEventDto emitEventDto) {
		Event event = new Event();
		event.getHeader().setFlowId("static-routing-pipeline");

		event.getBody().getMessage().setSubject(emitEventDto.getData().getMessageSubject());
		event.getBody().getMessage().setText(emitEventDto.getData().getMessageText());

		event.getBody().putAttributeValue(ORIGINAL_EVENT_FIELD, emitEventDto.asMap());
		return event;
	}

}