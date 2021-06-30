package com.obj.nc.functions.processors.event2Message;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.obj.nc.aspects.DocumentProcessingInfo;
import com.obj.nc.domain.event.GenericEvent;
import com.obj.nc.domain.message.Message;
import com.obj.nc.exceptions.PayloadValidationException;
import com.obj.nc.flows.inputEventRouting.extensions.GenericEvent2MessageProcessorExtension;
import com.obj.nc.flows.inputEventRouting.extensions.EventProcessorExtension;
import com.obj.nc.functions.processors.ProcessorFunctionAdapter;

@Component
@DocumentProcessingInfo("event2MessageExtension")
public class Event2MessageExtensionsConverter extends ProcessorFunctionAdapter<GenericEvent, List<Message<?>>> {
	
	@Autowired(required = false) 
	private List<EventProcessorExtension<?>> eventProcessors = new ArrayList<>();
	
	@Override
	protected Optional<PayloadValidationException> checkPreCondition(GenericEvent payload) {
		return 
			getMessageConverters().stream()
				.map(p -> p.checkPreCondition(payload))
				.findFirst().orElse(Optional.empty());
	}
	
	@Override
	protected List<Message<?>> execute(GenericEvent payload) {
		return 
				getMessageConverters().stream()
					.map(p -> p.convertEvent(payload))
					.flatMap(List::stream)
					.collect(Collectors.toList());
	}
	
	private List<GenericEvent2MessageProcessorExtension> getMessageConverters() {
		return eventProcessors.stream()
					.filter(p -> p instanceof GenericEvent2MessageProcessorExtension)
					.map(p -> (GenericEvent2MessageProcessorExtension)p)
					.collect(Collectors.toList());					
	}
	
}
