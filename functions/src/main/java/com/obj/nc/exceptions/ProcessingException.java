package com.obj.nc.exceptions;

public class ProcessingException extends RuntimeException {

	public ProcessingException(Class<?> processor, Exception e) {
		super("Processing exception ocured in the " + processor.getName(), e);
	}
	
	public ProcessingException(String message) {
		super(message);
	}
}
