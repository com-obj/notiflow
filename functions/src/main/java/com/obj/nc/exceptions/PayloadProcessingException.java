package com.obj.nc.exceptions;

public class PayloadProcessingException extends RuntimeException {

	public PayloadProcessingException(Class<?> processor, Exception e) {
		super("Processing exception ocured in the " + processor.getName(), e);
	}
}
