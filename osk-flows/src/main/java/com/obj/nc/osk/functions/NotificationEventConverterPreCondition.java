package com.obj.nc.osk.functions;

import com.obj.nc.exceptions.PayloadValidationException;
import com.obj.nc.functions.PreCondition;
import com.obj.nc.osk.sia.dto.IncidentTicketNotificationEventDto;

import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class NotificationEventConverterPreCondition implements PreCondition<IncidentTicketNotificationEventDto> {

	@Override
	public Optional<PayloadValidationException> apply(IncidentTicketNotificationEventDto emitEventDto) {
		if (emitEventDto == null) {
			return Optional.of(new PayloadValidationException("Notification event must not be null"));
		}

		return Optional.empty();
	}

}