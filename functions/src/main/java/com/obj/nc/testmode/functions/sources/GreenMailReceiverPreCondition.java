package com.obj.nc.testmode.functions.sources;

import com.obj.nc.domain.Messages;
import com.obj.nc.domain.message.Message;
import com.obj.nc.exceptions.PayloadValidationException;
import com.obj.nc.functions.PreCondition;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Log4j2
@Component
public class GreenMailReceiverPreCondition implements PreCondition<Messages> {

    @Override
    public Optional<PayloadValidationException> apply(Messages messages) {
        if (messages.getMessages().isEmpty()) {
            return Optional.of(new PayloadValidationException("There are no messages to supply"));
        }

        return Optional.empty();
    }

}
