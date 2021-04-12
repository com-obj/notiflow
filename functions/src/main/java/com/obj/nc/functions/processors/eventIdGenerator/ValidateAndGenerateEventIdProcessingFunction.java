package com.obj.nc.functions.processors.eventIdGenerator;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.obj.nc.aspects.DocumentProcessingInfo;
import com.obj.nc.domain.notifIntent.NotificationIntent;
import com.obj.nc.exceptions.PayloadValidationException;
import com.obj.nc.functions.processors.ProcessorFunctionAdapter;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Component
@AllArgsConstructor
@Log4j2
@DocumentProcessingInfo("ValidateAndGenerateEventId")
public class ValidateAndGenerateEventIdProcessingFunction
		extends ProcessorFunctionAdapter<NotificationIntent, NotificationIntent> {

	@Override
	protected Optional<PayloadValidationException> checkPreCondition(NotificationIntent payload) {
		return Optional.empty();
	}

	@Override
	protected NotificationIntent execute(NotificationIntent notificationIntent) {
		log.debug("Validating {}", notificationIntent);

		notificationIntent.getHeader().generateAndSetID();
		notificationIntent.getHeader().addEventId(notificationIntent.getHeader().getId());

		return notificationIntent;

	}

}