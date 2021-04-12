package com.obj.nc.aspects;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

import com.obj.nc.domain.headers.HasHeader;
import com.obj.nc.domain.headers.Header;
import com.obj.nc.domain.headers.NewProcessingInfoAppEvent;
import com.obj.nc.domain.headers.ProcessingInfo;

import lombok.extern.log4j.Log4j2;

@Aspect
@Component
@Log4j2
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
			log.trace("Exception ocured in processing step {}", docProcessingInfoAnot.value(), e);
			throw e;
		}
		
		List<ImmutablePair<Header, Object>> endPayloadAndHeaders = new ArrayList<>();
		endPayloadAndHeaders = extractPayloads(returnValue);
		
		if (startProcessingInfos.size()>1 /*&& endPayloadAndHeaders.size()>1*/) {
			log.warn("Cannot automaticaly map ProcessingInfo only for 1:1, 1:N cardinalities. Have {}:{} ", startPayloadAndHeaders.size(),endPayloadAndHeaders.size());
			return returnValue;
		}
		
		ProcessingInfo startProcessing = startProcessingInfos.size()==1? startProcessingInfos.get(0): null;
		Header startHeader = startPayloadAndHeaders.size()==1? startPayloadAndHeaders.get(0).getKey(): null;
	    
		List<Header> endHeaders = calculateEndProcessingInfos(endPayloadAndHeaders, startProcessing, startHeader);
		
		endHeaders.forEach( h-> {
				NewProcessingInfoAppEvent event = new NewProcessingInfoAppEvent(h);
				event.setReady(true);
				applicationEventPublisher.publishEvent(new NewProcessingInfoAppEvent(h));
			}
		);
		

		return returnValue;
	}

	private List<ProcessingInfo> calculateStartProcessingInfos(String stepName,
			List<ImmutablePair<Header, Object>> startPayloadAndHeaders) {
		List<ProcessingInfo> startProcessingInfos = new ArrayList<>();
		for (ImmutablePair<Header, Object> startPayloadAndHeader: startPayloadAndHeaders) {
			 Object startPayload = startPayloadAndHeader.getRight();
			 Header startHeader = startPayloadAndHeader.getLeft();
			    
		    ProcessingInfo startProcessing = ProcessingInfo.createProcessingInfoOnStepStart(
		    		stepName, startHeader, startPayload);
		    startHeader.setProcessingInfo(startProcessing);
		    
		    startProcessingInfos.add(startProcessing);
		}
		return startProcessingInfos;
	}

	private List<Header> calculateEndProcessingInfos(
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
				endHeader.generateAndSetID();
			}
			    
			ProcessingInfo endProcessing = ProcessingInfo.createProcessingInfoOnStepEnd(
					 startProcessing, endHeader, endPayload); 
		    
		    endHeaders.add(endHeader);
		    lastProcInfo = endProcessing;
		}
		
		String duration = lastProcInfo!=null ? lastProcInfo.getDurationInMs()+"" : "N/A";
		
		log.info("Processing finished for step {}. Took {} ms", startProcessing.getStepName(), duration);
		
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
		    ImmutablePair<Header, Object> headerPayloadPair = new ImmutablePair<>(header, input);
		    
		    result.add(headerPayloadPair);
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
		}
		//Add other options if needed
		
		log.warn("Cannot calculate processing info for return value fo type {}", input.getClass() );
		return result;
	}



}
