package com.obj.nc.functions.processors;

import java.util.function.Function;

import org.springframework.stereotype.Component;

import com.obj.nc.domain.event.Event;
import com.obj.nc.domain.message.Email;
import com.obj.nc.dto.EmitEventDto;

import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
public class KoderiaEventConverterExecution implements Function<EmitEventDto, Event> {

	public static final String ORIGINAL_EVENT_FIELD = "originalEvent";

	@Override
	public Event apply(EmitEventDto emitEventDto) {
		Event event = new Event();
		event.getHeader().setFlowId("static-routing-pipeline");
		event.getBody().setMessage(
				Email.createWithSubject(emitEventDto.getData().getMessageSubject(), emitEventDto.getData().getMessageText())
		);

		event.getBody().getMessage().setAttributeValue(ORIGINAL_EVENT_FIELD, emitEventDto.asMap());
		return event;
	}

}