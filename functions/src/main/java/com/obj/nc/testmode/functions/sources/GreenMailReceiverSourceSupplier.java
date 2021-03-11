package com.obj.nc.testmode.functions.sources;

import com.obj.nc.domain.Messages;
import com.obj.nc.domain.message.Message;
import com.obj.nc.functions.PreCondition;
import com.obj.nc.functions.sources.SourceSupplier;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Supplier;

@Component
@AllArgsConstructor
public class GreenMailReceiverSourceSupplier extends SourceSupplier<Messages> {

	@Autowired
	private GreenMailReceiverExecution execution;

	@Autowired
	private GreenMailReceiverPreCondition preCondition;

	@Override
	public PreCondition<Messages> preCondition() {
		return preCondition;
	}

	@Override
	public Supplier<Messages> execution() {
		return execution;
	}

}
