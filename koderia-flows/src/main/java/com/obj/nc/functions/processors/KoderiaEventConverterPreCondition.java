package com.obj.nc.functions.processors;

import com.obj.nc.dto.EmitEventDto;
import com.obj.nc.exceptions.PayloadValidationException;
import com.obj.nc.functions.PreCondition;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class KoderiaEventConverterPreCondition implements PreCondition<EmitEventDto> {

	@Override
	public Optional<PayloadValidationException> apply(EmitEventDto emitEventDto) {
		if (emitEventDto == null) {
			return Optional.of(new PayloadValidationException("Koderia event must not be null"));
		}

		if (emitEventDto.getSubject() == null) {
			return Optional.of(new PayloadValidationException("Subject of Koderia event must not be null"));
		}

		if (emitEventDto.getText() == null) {
			return Optional.of(new PayloadValidationException("Text of Koderia event must not be null"));
		}

		return Optional.empty();
	}

}