package com.obj.nc.functions.sink.failedPaylodPersister;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.obj.nc.flows.errorHandling.domain.FailedPayload;
import com.obj.nc.functions.sink.SinkConsumerAdapter;
import com.obj.nc.repositories.FailedPayloadRepository;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Component
@AllArgsConstructor
@Log4j2
public class FailedPayloadPersister extends SinkConsumerAdapter<FailedPayload> {

    @Autowired
    private FailedPayloadRepository failedPayloadRepo;


	@Override
	protected void execute(FailedPayload failedPaylod) {
		try {
			failedPayloadRepo.save(failedPaylod);
		} catch (Exception e) {
			log.error("Error ocured in Error handling flow: ", e);
			throw e;
		}
	}

}
