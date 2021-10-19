/*
 *   Copyright (C) 2021 the original author or authors.
 *
 *   This file is part of Notiflow
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Lesser General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public License
 *   along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.obj.nc.functions.processors.event2Message;

import com.obj.nc.aspects.DocumentProcessingInfo;
import com.obj.nc.converterExtensions.genericEvent.InputEventConverterExtension;
import com.obj.nc.domain.IsNotification;
import com.obj.nc.domain.event.GenericEvent;
import com.obj.nc.exceptions.PayloadValidationException;
import com.obj.nc.functions.processors.ProcessorFunctionAdapter;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@DocumentProcessingInfo("ExtensionsBasedEventConvertor")
@AllArgsConstructor
@Slf4j
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
					.map(p -> p.convert(payload))
					.peek(notifications -> notifications.stream()
						.filter(notification -> !notification.getPreviousEventIds().contains(payload.getEventId()))
						.forEach(notification -> notification.addPreviousEventId(payload.getId())))
					.flatMap(List::stream)
					.collect(Collectors.toList());
	}

	
}
