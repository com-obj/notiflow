package com.obj.nc.functions.processors.eventIdGenerator;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.obj.nc.aspects.DocumentProcessingInfo;
import com.obj.nc.domain.headers.HasHeader;
import com.obj.nc.exceptions.PayloadValidationException;
import com.obj.nc.functions.processors.ProcessorFunctionAdapter;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Component
@AllArgsConstructor
@Log4j2
@DocumentProcessingInfo("GenerateEventId")
/**
 * Not sure if this is needed. Standard flow is to recieve event via nc_input and use that eventId throu out the processing
 * @author ja
 *
 */
@Deprecated
public class GenerateEventIdProcessingFunction
		extends ProcessorFunctionAdapter<HasHeader, HasHeader> {

	@Override
	protected Optional<PayloadValidationException> checkPreCondition(HasHeader payload) {
		return Optional.empty();
	}

	@Override
	protected HasHeader execute(HasHeader payload) {
		log.debug("Validating {}", payload);

		payload.getHeader().addEventId(UUID.randomUUID());

		return payload;

	}

}