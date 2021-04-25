package com.obj.nc.koderia.functions.processors;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.obj.nc.exceptions.PayloadValidationException;
import com.obj.nc.functions.PreCondition;
import com.obj.nc.koderia.dto.EmitEventDto;

@Component
public class KoderiaEventConverterPreCondition implements PreCondition<EmitEventDto> {

	@Override
	public Optional<PayloadValidationException> apply(EmitEventDto emitEventDto) {
		if (emitEventDto == null) {
			return Optional.of(new PayloadValidationException("Koderia event must not be null"));
		}

		if (emitEventDto.getData().getMessageSubject() == null) {
			return Optional.of(new PayloadValidationException("Subject of Koderia event must not be null"));
		}

		if (emitEventDto.getData().getMessageText() == null) {
			return Optional.of(new PayloadValidationException("Text of Koderia event must not be null"));
		}

		return Optional.empty();
	}

}