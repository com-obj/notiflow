package com.obj.nc.koderia.functions.processors;

import java.util.function.Function;

import org.springframework.stereotype.Component;

import com.obj.nc.domain.content.email.EmailContent;
import com.obj.nc.domain.notifIntent.NotificationIntent;
import com.obj.nc.koderia.dto.EmitEventDto;

import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
public class KoderiaEventConverterExecution implements Function<EmitEventDto, NotificationIntent> {

	public static final String ORIGINAL_EVENT_FIELD = "originalEvent";

	@Override
	public NotificationIntent apply(EmitEventDto emitEventDto) {
		NotificationIntent notificationIntent = new NotificationIntent();
		notificationIntent.getHeader().setFlowId("static-routing-pipeline");
		notificationIntent.getBody().setMessage(
				EmailContent.createWithSubject(emitEventDto.getData().getMessageSubject(), emitEventDto.getData().getMessageText())
		);

		notificationIntent.getBody().getMessage().setAttributeValue(ORIGINAL_EVENT_FIELD, emitEventDto.asMap());
		return notificationIntent;
	}

}