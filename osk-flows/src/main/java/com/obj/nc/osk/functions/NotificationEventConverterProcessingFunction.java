package com.obj.nc.osk.functions;

import java.util.function.Function;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.obj.nc.domain.event.Event;
import com.obj.nc.functions.PreCondition;
import com.obj.nc.functions.processors.ProcessorFunction;
import com.obj.nc.osk.sia.dto.IncidentTicketNotificationEventDto;

import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
public class NotificationEventConverterProcessingFunction extends ProcessorFunction<IncidentTicketNotificationEventDto, Event> {

	@Autowired
	private NotificationEventConverterExecution execution;

	@Autowired
	private NotificationEventConverterPreCondition preCondition;

	@Override
	public PreCondition<IncidentTicketNotificationEventDto> preCondition() {
		return preCondition;
	}

	@Override
	public Function<IncidentTicketNotificationEventDto, Event> execution() {
		return execution;
	}

}
