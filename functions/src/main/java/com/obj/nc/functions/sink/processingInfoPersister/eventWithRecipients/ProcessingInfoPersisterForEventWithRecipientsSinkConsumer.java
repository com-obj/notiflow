package com.obj.nc.functions.sink.processingInfoPersister.eventWithRecipients;

import com.obj.nc.domain.BasePayload;
import com.obj.nc.functions.PreCondition;
import com.obj.nc.functions.sink.SinkConsumer;
import com.obj.nc.functions.sink.processingInfoPersister.ProcessingInfoPersisterPreCondition;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

@Component
@AllArgsConstructor
public class ProcessingInfoPersisterForEventWithRecipientsSinkConsumer extends SinkConsumer<BasePayload> {

	@Autowired
	private ProcessingInfoPersisterForEventWithRecipientsExecution execution;

	@Autowired
	private ProcessingInfoPersisterPreCondition preCondition;

	@Override
	public PreCondition<BasePayload> preCondition() {
		return preCondition;
	}

	@Override
	public Consumer<BasePayload> execution() {
		return execution;
	}

}
