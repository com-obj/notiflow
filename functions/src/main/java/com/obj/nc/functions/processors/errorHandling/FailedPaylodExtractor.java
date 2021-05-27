package com.obj.nc.functions.processors.errorHandling;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.obj.nc.config.SpringIntegration;
import com.obj.nc.exceptions.PayloadValidationException;
import com.obj.nc.flows.errorHandling.domain.FailedPaylod;
import com.obj.nc.functions.processors.ProcessorFunctionAdapter;

import lombok.SneakyThrows;

@Component
public class FailedPaylodExtractor extends ProcessorFunctionAdapter<FailedPaylod, Message<?>> {

	@Autowired @Qualifier(SpringIntegration.OBJECT_MAPPER_FOR_SPRING_MESSAGES_BEAN_NAME)
	private ObjectMapper jsonConverterForMessages;
	
	@Override
	protected Optional<PayloadValidationException> checkPreCondition(FailedPaylod payload) {
		return super.checkPreCondition(payload);
	}
	
	@Override
	@SneakyThrows
	protected Message<?> execute(FailedPaylod failedPaylod) {
		Message<?> failedMsg = jsonConverterForMessages.treeToValue(failedPaylod.getMessageJson(), Message.class);
		return failedMsg;
	}

}
