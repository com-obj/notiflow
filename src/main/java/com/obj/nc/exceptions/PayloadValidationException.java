package com.obj.nc.exceptions;

import javax.validation.ConstraintViolation;

public class PayloadValidationException extends RuntimeException {

	public PayloadValidationException(String message) {
		super(message);
	}
	
	public PayloadValidationException(ConstraintViolation< ?> violation) {
		super(violation.getRootBeanClass() +"." + violation.getPropertyPath()+": " +violation.getMessage());
	}
}
