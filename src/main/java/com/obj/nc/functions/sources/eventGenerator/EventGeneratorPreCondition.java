package com.obj.nc.functions.sources.eventGenerator;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.obj.nc.domain.notifIntent.NotificationIntent;
import com.obj.nc.exceptions.PayloadValidationException;
import com.obj.nc.functions.PreCondition;

@Component
public class EventGeneratorPreCondition implements PreCondition<NotificationIntent> {

    @Override
    public Optional<PayloadValidationException> apply(NotificationIntent notificationIntent) {
        if (notificationIntent == null) {
            return Optional.of(new PayloadValidationException("Input NotificationIntent must not be null"));
        }

        return Optional.empty();
    }

}
