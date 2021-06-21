package com.obj.nc.functions.sink.processingInfoPersister;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.obj.nc.domain.BasePayload;
import com.obj.nc.domain.headers.ProcessingInfo;
import com.obj.nc.exceptions.PayloadValidationException;
import com.obj.nc.functions.sink.SinkConsumerAdapter;
import com.obj.nc.repositories.ProcessingInfoRepository;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Component
@AllArgsConstructor
@Log4j2
@Deprecated
public class ProcessingInfoPersisterSinkConsumer extends SinkConsumerAdapter<BasePayload> {

    @Autowired
    private ProcessingInfoRepository piRepo;
    
	@Override
	protected Optional<PayloadValidationException> checkPreCondition(BasePayload payload) {
		ProcessingInfo processingInfo = payload.getHeader().getProcessingInfo();

		if (processingInfo == null) {
			return Optional.of(new PayloadValidationException("Could not persist ProcessingInfo because the payload didn't contain it. Payload: " + payload));
		}

		return Optional.empty();
	}

	@Override
	protected void execute(BasePayload payload) {
		piRepo.save(payload.getHeader().getProcessingInfo());
	}
	

}
