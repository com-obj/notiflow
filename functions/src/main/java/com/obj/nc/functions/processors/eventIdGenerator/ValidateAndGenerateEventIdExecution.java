package com.obj.nc.functions.processors.eventIdGenerator;

import java.util.function.Function;

import org.springframework.stereotype.Component;

import com.obj.nc.aspects.DocumentProcessingInfo;
import com.obj.nc.domain.notifIntent.NotificationIntent;

import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
public class ValidateAndGenerateEventIdExecution implements Function<NotificationIntent, NotificationIntent> {

	@DocumentProcessingInfo("ValidateAndGenerateEventId")
	public NotificationIntent apply(NotificationIntent notificationIntent) {
		log.debug("Validating {}",  notificationIntent);
		
		notificationIntent.getHeader().generateAndSetID();
		notificationIntent.getHeader().addEventId(notificationIntent.getHeader().getId());

		return notificationIntent;
	}
}