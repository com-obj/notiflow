package com.obj.nc.flows.inputRouting;

import java.util.Arrays;
import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.router.AbstractMessageRouter;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;

import com.fasterxml.jackson.databind.JsonNode;
import com.obj.nc.Get;
import com.obj.nc.domain.HasJsonPayload;
import com.obj.nc.exceptions.PayloadValidationException;
import com.obj.nc.flows.inputRouting.config.InputEventRoutingProperties;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class SimpleTypeBasedMessageRouter extends AbstractMessageRouter {
	
	@Autowired private InputEventRoutingProperties props;
	
	@Override
	protected Collection<MessageChannel> determineTargetChannels(Message<?> message) {
		if (!(message.getPayload() instanceof HasJsonPayload)) {
			throw new PayloadValidationException(this.getClass().getName() + " can only be used to route messages of type HasJsonPayload. Current message is " + message.getPayload());
		}
		
		JsonNode payload = ((HasJsonPayload)message.getPayload()).getPayloadJson();
			
		try {
			String jsonTypeInfo = payload.get(props.getTypeProperyName()).textValue();
			
			MessageChannel inputChannel = Get.getBean(props.getTypeChannelMapping().get(jsonTypeInfo), MessageChannel.class);
			return Arrays.asList(inputChannel);
		} catch (Exception ex) {
			log.error(ex);
			
			throw new PayloadValidationException("Could not extract channel name from payload type and channel mapping. Exprecting property " +  props.getTypeProperyName() + " to contain type info which maps to channel name as defined using  "
					+ "nc.flows.input-evet-routing.type-channel-mapping which is " + props.getTypeChannelMapping());	
		}
	}
}