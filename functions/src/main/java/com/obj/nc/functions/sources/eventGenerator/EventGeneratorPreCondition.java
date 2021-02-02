package com.obj.nc.functions.sources.eventGenerator;

import com.obj.nc.domain.event.Event;
import com.obj.nc.exceptions.PayloadValidationException;
import com.obj.nc.functions.PreCondition;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@Log4j2
@Profile("dev")
public class EventGeneratorPreCondition implements PreCondition<Event> {

    @Override
    public Optional<PayloadValidationException> apply(Event event) {
        if (event == null) {
            return Optional.of(new PayloadValidationException("Input Event must not be null"));
        }

        return Optional.empty();
    }

}
