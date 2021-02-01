package com.obj.nc.functions.sink.processingInfoPersister;

import com.obj.nc.domain.BasePayload;
import com.obj.nc.domain.ProcessingInfo;
import com.obj.nc.domain.message.Message;
import com.obj.nc.exceptions.PayloadValidationException;
import com.obj.nc.exceptions.ProcessingException;
import com.obj.nc.functions.PreCondition;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class ProcessingInfoPersisterPreCondition implements PreCondition<BasePayload> {

	@Override
	public Optional<PayloadValidationException> apply(BasePayload payload) {
		ProcessingInfo processingInfo = payload.getProcessingInfo();

		if (processingInfo == null) {
			return Optional.of(new PayloadValidationException("Could not persist ProcessingInfo because the payload didn't contain it. Payload: " + payload));
		}

		return Optional.empty();
	}

}