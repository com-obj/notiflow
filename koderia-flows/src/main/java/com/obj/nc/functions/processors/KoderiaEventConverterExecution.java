package com.obj.nc.functions.processors;

import com.obj.nc.domain.event.Event;
import com.obj.nc.dto.EmitEventDto;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import java.util.function.Function;

@Component
@Log4j2
public class KoderiaEventConverterExecution implements Function<EmitEventDto, Event> {

	@Override
	public Event apply(EmitEventDto emitEventDto) {
		Event event = new Event();
		event.getHeader().setConfigurationName("static-routing-pipeline");

		event.getBody().getMessage().getContent().setSubject(emitEventDto.getSubject());
		event.getBody().getMessage().getContent().setText(emitEventDto.getText());

		event.getBody().setAttributes(emitEventDto.asMap());
		return event;
	}

}