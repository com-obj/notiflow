package com.obj.nc.functions.sources.eventGenerator;

import com.obj.nc.domain.notifIntent.NotificationIntent;
import com.obj.nc.exceptions.PayloadValidationException;
import com.obj.nc.functions.PreCondition;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@Log4j2
public class EventGeneratorPreCondition implements PreCondition<NotificationIntent> {

    @Override
    public Optional<PayloadValidationException> apply(NotificationIntent notificationIntent) {
        if (notificationIntent == null) {
            return Optional.of(new PayloadValidationException("Input NotificationIntent must not be null"));
        }

        return Optional.empty();
    }

}
