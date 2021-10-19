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

package com.obj.nc.aspects;

import com.obj.nc.domain.headers.HasHeader;
import com.obj.nc.domain.headers.Header;
import com.obj.nc.domain.headers.NewProcessingInfoAppEvent;
import com.obj.nc.domain.headers.ProcessingInfo;
import com.obj.nc.repositories.ProcessingInfoRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.Message;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Aspect
@Component
@Slf4j
public class ProcessingInfoGenerator {

//	@Pointcut("within(@com.obj.nc.aspects.DocumentProcessingInfo *)")
//	public void clssAnnotatedWithDocumentProcessingInfo() {}
//
	@Pointcut("execution(* com.obj.nc.functions.processors.ProcessorFunction.apply(..))")
	public void processorExecution() {}

	@Pointcut("execution(* com.obj.nc.functions.sink.SinkConsumer.accept(..))")
	public void sinkExecution() {}
	
	@Pointcut("execution(* com.obj.nc.functions.sources.SourceSupplier.get(..))")
	public void sourceExecution() {}
	
	@Pointcut("processorExecution() || sinkExecution() || sourceExecution()")
	public void endpointExecutions() {}
	
    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;
    @Autowired
    private ProcessingInfoRepository piRepo;
	
	@Around("endpointExecutions()")
	public Object updateProcessingInfoOnPayload(ProceedingJoinPoint joinPoint) throws Throwable {
	    DocumentProcessingInfo docProcessingInfoAnot = joinPoint.getTarget().getClass().getAnnotation(DocumentProcessingInfo.class);

		if (docProcessingInfoAnot == null) {
			return joinPoint.proceed();
		}
		
		List<ImmutablePair<Header, Object>> startPayloadAndHeaders = new ArrayList<>();
		if (joinPoint.getArgs().length == 1) {
			startPayloadAndHeaders = extractPayloads(joinPoint.getArgs()[0]);
		}
		
		List<ProcessingInfo> startProcessingInfos = calculateStartProcessingInfos(
				docProcessingInfoAnot.value(),
				startPayloadAndHeaders);
		
		Object returnValue = null;
		try {
			returnValue = joinPoint.proceed();
		} catch (Throwable e) {
			log.trace("Exception ocurred in processing step {}", docProcessingInfoAnot.value(), e);
			throw e;
		}
		
		List<ImmutablePair<Header, Object>> endPayloadAndHeaders = new ArrayList<>();
		endPayloadAndHeaders = extractPayloads(returnValue);
		
		if (startProcessingInfos.size()>1 /*&& endPayloadAndHeaders.size()>1*/) {
			log.warn("Cannot automatically map ProcessingInfo only for 1:1, 1:N cardinalities. Have {}:{} ", startPayloadAndHeaders.size(),endPayloadAndHeaders.size());
			return returnValue;
		}
		
		ProcessingInfo startProcessing = startProcessingInfos.size()==1? startProcessingInfos.get(0): null;
		Header startHeader = startPayloadAndHeaders.size()==1? startPayloadAndHeaders.get(0).getKey(): null;
	    
		List<Header> endHeaders = calculateEndHeaders(endPayloadAndHeaders, startProcessing, startHeader);
		
		endHeaders.forEach( h-> {
				NewProcessingInfoAppEvent event = new NewProcessingInfoAppEvent(h.getProcessingInfo());
				event.setReady(true);
				applicationEventPublisher.publishEvent(event);
			}
		);
		

		return returnValue;
	}

	private List<ProcessingInfo> calculateStartProcessingInfos(String stepName,
			List<ImmutablePair<Header, Object>> startPayloadAndHeaders) {
		List<ProcessingInfo> startProcessingInfos = new ArrayList<>();
		
		if (startPayloadAndHeaders.size() == 0) {
			//suppliers
			
			ProcessingInfo startProcessing = ProcessingInfo.createProcessingInfoOnStepStart(
		    		stepName, null, null);
			
			startProcessingInfos.add(startProcessing);
			return startProcessingInfos;
		}
		
		//oters
		for (ImmutablePair<Header, Object> startPayloadAndHeader: startPayloadAndHeaders) {
			 Object startPayload = startPayloadAndHeader.getRight();
			 Header startHeader = startPayloadAndHeader.getLeft();
			    
		    ProcessingInfo startProcessing = ProcessingInfo.createProcessingInfoOnStepStart(
		    		stepName, startHeader.getProcessingInfo(), startPayload);
		    
		    startProcessingInfos.add(startProcessing);
		}
		return startProcessingInfos;
	}

	private List<Header> calculateEndHeaders(
			List<ImmutablePair<Header, Object>> endPayloadAndHeaders,
			ProcessingInfo startProcessing, Header startHeader) {
		
		List<Header> endHeaders = new ArrayList<>();
		
		ProcessingInfo lastProcInfo = null;
		for (ImmutablePair<Header, Object> endPayloadAndHeader: endPayloadAndHeaders) {
			Object endPayload = endPayloadAndHeader.getRight();
			Header endHeader = endPayloadAndHeader.getLeft();
			
			
			boolean startAndEndBeanDifferent = startHeader != endHeader;
			if (startAndEndBeanDifferent) {
				endHeader.copyHeaderFrom(startHeader);
			}
			    
			ProcessingInfo endProcessing = ProcessingInfo.createProcessingInfoOnStepEnd(
					 startProcessing, endHeader, endPayload); 
		    
		    endHeaders.add(endHeader);
		    lastProcInfo = endProcessing;
		}
		
		String duration = lastProcInfo!=null ? lastProcInfo.getStepDurationMs()+"" : "N/A";
		
		log.debug("Processing finished for step {}. Took {} ms", startProcessing.getStepName(), duration);
		
		return endHeaders;
	}
	
	private List<ImmutablePair<Header, Object>> extractPayloads(Object input) {

		List<ImmutablePair<Header, Object>> result = new ArrayList<>();
		if (input== null) {
			//we should record that flow ended after this step
			return result;
		}
		
		if (input instanceof HasHeader) {
		    Header header = ((HasHeader)input).getHeader();
		    if (!header.isSuppressGenerateProcessingInfo()) {
			    ImmutablePair<Header, Object> headerPayloadPair = new ImmutablePair<>(header, input);
			    
			    result.add(headerPayloadPair);
		    }
		} else if (input instanceof Iterable) {
			Iterator<?> iterator = ((Iterable<?>)input).iterator();
			while (iterator.hasNext()) {
				Object singleInput = iterator.next();
				
				List<ImmutablePair<Header, Object>> pairs = extractPayloads(singleInput);
				result.addAll(pairs);
			}
		} else if (input instanceof Message<?>) {
			Message<?> springMessage = (Message<?>)input;
			List<ImmutablePair<Header, Object>> pairs = extractPayloads(springMessage.getPayload());
			
			result.addAll(pairs);
		} else {
		//Add other options if needed
			log.warn("Cannot calculate processing info for return value fo type {}", input.getClass() );
		}

		return result;
	}
	
	
    @Async
    @EventListener
	public void persistPIFromEvent(@Validated NewProcessingInfoAppEvent event) {
		log.debug("Recieved NewProcessingInfoAppEvent: {}", event);
		if (!event.isReady()) {
			return;
		}
		
		piRepo.save(event.getPi());
	}




}
