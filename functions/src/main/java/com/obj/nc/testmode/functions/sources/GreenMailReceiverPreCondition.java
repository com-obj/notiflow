package com.obj.nc.testmode.functions.sources;

import com.obj.nc.domain.message.Message;
import com.obj.nc.exceptions.PayloadValidationException;
import com.obj.nc.functions.PreCondition;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@Log4j2
@ConditionalOnProperty(value = "testmode.enabled", havingValue = "true")
public class GreenMailReceiverPreCondition implements PreCondition<List<Message>> {

    @Override
    public Optional<PayloadValidationException> apply(List<Message> messages) {
        if (messages.isEmpty()) {
            return Optional.of(new PayloadValidationException("There are no messages to supply"));
        }

        return Optional.empty();
    }

}
