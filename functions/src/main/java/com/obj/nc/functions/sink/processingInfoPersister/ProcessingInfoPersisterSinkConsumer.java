package com.obj.nc.functions.sink.processingInfoPersister;

import com.obj.nc.domain.BasePayload;
import com.obj.nc.functions.PreCondition;
import com.obj.nc.functions.sink.SinkConsumer;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

@Component
@AllArgsConstructor
public class ProcessingInfoPersisterSinkConsumer extends SinkConsumer<BasePayload> {

	@Autowired
	private ProcessingInfoPersisterExecution execution;

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
