package com.obj.nc.functions.processors.event2Message;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;

import com.obj.nc.aspects.DocumentProcessingInfo;
import com.obj.nc.domain.IsNotification;
import com.obj.nc.domain.event.GenericEvent;
import com.obj.nc.exceptions.PayloadValidationException;
import com.obj.nc.flows.inputEventRouting.extensions.InputEventConverterExtension;
import com.obj.nc.functions.processors.ProcessorFunctionAdapter;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;

@DocumentProcessingInfo("ExtensionsBasedEventConvertor")
@AllArgsConstructor
@Log4j2
public class ExtensionsBasedEventConvertor extends ProcessorFunctionAdapter<GenericEvent, List<IsNotification>> {
	
	@Autowired(required = false) 
	private List<InputEventConverterExtension<? extends IsNotification>> eventProcessors = new ArrayList<>();
	
	@Override
	protected Optional<PayloadValidationException> checkPreCondition(GenericEvent payload) {
		List<InputEventConverterExtension<?>> processors = findMatchingEventProcessors(payload);
		if (processors.size() > 0) {
			return Optional.empty();
		}
		
		return Optional.of(new PayloadValidationException("No EventProcessorExtension is able to handle " + payload + ". The paylod won't be transformed and thus will not be processed"));
	}
	
	private List<InputEventConverterExtension<?>> findMatchingEventProcessors(GenericEvent payload) {
		List<InputEventConverterExtension<?>> matchingProcessors = new ArrayList<InputEventConverterExtension<?>>();
		
		for (InputEventConverterExtension<? extends IsNotification> p: eventProcessors) {
			Optional<PayloadValidationException> errors = p.canHandle(payload);
			if (!errors.isPresent()) {
				matchingProcessors.add(p);
				continue;
			} 
			
			if (log.isDebugEnabled()) {
				log.debug("ExtensionsBasedEventConvertor examined event processor which cannot handle payload " + payload + ". Processor replyed" + errors.get().getMessage());
			}
		}

		return matchingProcessors;
	}
	
	@Override
	protected List<IsNotification> execute(GenericEvent payload) {
		return 
				findMatchingEventProcessors(payload).stream()
					.map(p -> p.convertEvent(payload))
					.peek(notifications -> notifications
						.forEach(notification -> notification.addPreviousEventId(payload.getId())))
					.flatMap(List::stream)
					.collect(Collectors.toList());
	}

	
}
