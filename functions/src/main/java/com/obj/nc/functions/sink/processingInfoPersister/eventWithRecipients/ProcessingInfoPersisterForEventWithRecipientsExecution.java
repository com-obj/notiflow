package com.obj.nc.functions.sink.processingInfoPersister.eventWithRecipients;

import com.obj.nc.domain.BasePayload;
import com.obj.nc.domain.endpoints.RecievingEndpoint;
import com.obj.nc.functions.sink.processingInfoPersister.ProcessingInfoPersisterExecution;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

@Component
@Log4j2
public class ProcessingInfoPersisterForEventWithRecipientsExecution
        extends ProcessingInfoPersisterExecution implements Consumer<BasePayload> {

    @Override
    public void accept(BasePayload payload) {
        super.accept(payload);

        List<RecievingEndpoint> recipients = payload.getBody().getRecievingEndpoints();
        UUID processingId = payload.getProcessingInfo().getProcessingId();

        persistEnpointIfNotExists(recipients);
        persistEnpoint2Processing(processingId, recipients);
    }

}