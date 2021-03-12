package com.obj.nc.osk.functions;

import java.util.function.Function;

import org.springframework.stereotype.Component;

import com.obj.nc.domain.event.Event;
import com.obj.nc.osk.sia.dto.IncidentTicketNotificationEventDto;

import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
public class NotificationEventConverterExecution implements Function<IncidentTicketNotificationEventDto, Event> {

	public static final String ORIGINAL_EVENT_FIELD = "originalEvent";

	@Override
	public Event apply(IncidentTicketNotificationEventDto emitEventDto) {
		Event event = new Event();
		event.getHeader().setFlowId("static-routing-pipeline");

//		event.getBody().getMessage().getContent().setSubject(emitEventDto.getSubject());
//		event.getBody().getMessage().getContent().setText(emitEventDto.getText());

//		event.getBody().putAttributeValue(ORIGINAL_EVENT_FIELD, emitEventDto.asMap());
		return event;
	}

}