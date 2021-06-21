package com.obj.nc.flows.inputEventRouting;

import java.util.Arrays;
import java.util.Collection;

import org.springframework.integration.router.AbstractMessageRouter;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;

import com.obj.nc.Get;
import com.obj.nc.domain.HasFlowId;
import com.obj.nc.exceptions.PayloadValidationException;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class FlowId2InputMessageRouter extends AbstractMessageRouter {
	@Override
	protected Collection<MessageChannel> determineTargetChannels(Message<?> message) {
		if (!(message.getPayload() instanceof HasFlowId)) {
			throw new PayloadValidationException(this.getClass().getName() + " can only be used to route messages which implement HasFlowId. Current message is " + message.getPayload());
		}
		
		String flowId = ((HasFlowId)message.getPayload()).getFlowId();
		
		try {
			MessageChannel inputChannel = Get.getBean(flowId + "_INPUT", MessageChannel.class);
			return Arrays.asList(inputChannel);
		} catch (Exception ex) {
			log.error(ex);
			
			throw new PayloadValidationException("No chanel with name " + flowId + "_INPUT found. "
					+ "Adjust the flow name to match you flow input chanel or consider turning input event routing off by deleting nc.flows.input-evet-routing.type property");	
		}
	}
}