/*
 *   Copyright (C) 2021 the original author or authors.
 *
 *   This file is part of Notiflow
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.obj.nc.flows.inputEventRouting;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.integration.router.AbstractMessageRouter;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;

import com.fasterxml.jackson.databind.JsonNode;
import com.obj.nc.Get;
import com.obj.nc.domain.HasFlowId;
import com.obj.nc.domain.HasJsonPayload;
import com.obj.nc.domain.event.GenericEvent;
import com.obj.nc.flows.inputEventRouting.config.InputEventExtensionConvertingFlowConfig;
import com.obj.nc.flows.inputEventRouting.config.InputEventRoutingProperties;

import lombok.extern.log4j.Log4j2;

@Log4j2
/**
 * Conversion from custom input event to anything which NC can handle might be very specific. This router enables to customize the event->intent or event->message conversion
 * It can be done it three ways
 * 1) register a bean implementing GenericEventProcessorExtension - this way you don't need to do anything related to spring integration 
 * 2) configure via application.properties flowId to channel name mapping
 * 3) configure via application.properties payload attribute which is used to create channel name mapping
 * @author Jan Cuzy
 *
 */
public class InputEventRouter extends AbstractMessageRouter {
	
	@Autowired private InputEventRoutingProperties props;
	
	@Qualifier(InputEventExtensionConvertingFlowConfig.EVENT_CONVERTING_EXTENSION_FLOW_ID_INPUT_CHANNEL_ID)
	@Autowired private MessageChannel extensionBasedConversionInputChannel;
	
	@Override
	protected Collection<MessageChannel> determineTargetChannels(Message<?> message) {
		Optional<String> flowId = extractFlowId(message);
		if (flowId.isPresent()) {
			
			Optional<Collection<MessageChannel>> routing =  flowBaseRouting(flowId.get());
			
			if (routing.isPresent()) {
				return routing.get();
			}
		}
		
		Optional<String> payloadTypeId = extractPayloadType(message);
		if (payloadTypeId.isPresent()) {
			
			Optional<Collection<MessageChannel>> routing = typeBasedRouting(payloadTypeId.get());
			
			if (routing.isPresent()) {
				return routing.get();
			}
		} 
		
		return Arrays.asList(extensionBasedConversionInputChannel);
	}
	
	protected Optional<Collection<MessageChannel>> typeBasedRouting(String jsonTypeInfo) {			
		try {
			MessageChannel inputChannel = Get.getBean(props.getTypeChannelMapping().get(jsonTypeInfo), MessageChannel.class);
			return Optional.of(Arrays.asList(inputChannel));
		} catch (Exception ex) {
			log.info("Could not extract channel name from payload type and channel mapping. Expecting property " +  props.getTypeProperyName() + " to contain type info which maps to channel name as defined using  "
					+ "nc.flows.input-event-routing.type-channel-mapping which is " + props.getTypeChannelMapping());
			return Optional.empty();
		}
	}	
	
	protected Optional<Collection<MessageChannel>> flowBaseRouting(String flowId) {
		
		try {
			MessageChannel inputChannel = Get.getBean(flowId + "_INPUT", MessageChannel.class);
			return Optional.of(Arrays.asList(inputChannel));
		} catch (Exception ex) {
			log.info("No chanel with name " + flowId + "_INPUT found. "
					+ "Adjust the flow name to match you flow input chanel or consider turning input event routing off by deleting nc.flows.input-event-routing.type property");

			return Optional.empty();
		}
	}
	
	private Optional<String> extractFlowId(Message<?> message) {
		if (!(message.getPayload() instanceof HasFlowId)) {
			return Optional.empty();
		}
		
		HasFlowId payload = (HasFlowId)message.getPayload();
		
		if (GenericEvent.DEFAULT_FLOW_ID.equals(payload.getFlowId())) {
			return Optional.empty();
		}
		
		return Optional.of(payload.getFlowId());
	}
	
	private Optional<String> extractPayloadType(Message<?> message) {
		if (!(message.getPayload() instanceof HasJsonPayload)) {
			return Optional.empty();
		}
		
		HasJsonPayload payload = (HasJsonPayload)message.getPayload();
		JsonNode jsonPayload = payload.getPayloadJson();
		JsonNode nodeValue = jsonPayload.get(props.getTypeProperyName());
		if (nodeValue == null) {
			log.info("No value in payload for attribute " + props.getTypeProperyName() +". Not using payloadType based event routing.");
			return Optional.empty();
		}
		String jsonTypeInfo = jsonPayload.get(props.getTypeProperyName()).textValue();
		
		return Optional.of(jsonTypeInfo);
	}

	

}