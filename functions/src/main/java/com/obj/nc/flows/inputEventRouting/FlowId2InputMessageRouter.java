package com.obj.nc.flows.inputEventRouting;

import java.util.Arrays;
import java.util.Collection;

import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.integration.router.AbstractMessageRouter;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;

import com.obj.nc.Get;
import com.obj.nc.domain.HasFlowId;
import com.obj.nc.exceptions.PayloadValidationException;

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
			throw new PayloadValidationException("No chanel with name " + flowId + "_INPUT found. Adjust the flow name to match you flow input chanel or consider turnin input event routing off by setting nc.flows.input-evet-routing.enabled=false");	
		}
	}
}