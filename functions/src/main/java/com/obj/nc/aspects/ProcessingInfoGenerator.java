package com.obj.nc.aspects;

import java.lang.reflect.Method;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import com.obj.nc.domain.BasePayload;
import com.obj.nc.exceptions.ProcessingException;

import lombok.extern.log4j.Log4j2;

@Aspect
@Component
@Log4j2
public class ProcessingInfoGenerator {

	@Around("@annotation(DocumentProcessingInfo)")
	public Object updateProcessingInfoOnPayload(ProceedingJoinPoint joinPoint) throws Throwable {
		if (joinPoint.getArgs().length != 1 || !(joinPoint.getArgs()[0] instanceof BasePayload)) {
			throw new ProcessingException("@DocumentProcessingInfo can be used onlu on methods with single parameter which is of type BasePayload or its subtypes. Called on " + joinPoint.getSignature());
		}
		
		MethodSignature signature = (MethodSignature) joinPoint.getSignature();
	    Method method = signature.getMethod();

	    DocumentProcessingInfo myAnnotation = method.getAnnotation(DocumentProcessingInfo.class);
		log.info("Generating processing info for step {}", myAnnotation.value());
		
		BasePayload payload = (BasePayload)joinPoint.getArgs()[0];
		payload.stepStart(myAnnotation.value());
		
		Object returnValue = null;
		try {
			returnValue = joinPoint.proceed();
		} catch (Throwable e) {
			log.error("Exception ocured in processing step " + myAnnotation.value(), e);
			throw e;
		}
		
		payload.stepFinish();

		return returnValue;
		
	}

}
