package com.obj.nc.aspects;

import static java.lang.annotation.ElementType.METHOD;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DocumentProcessingInfo {
	
	String value() default "SET_STEP_NAME_TO_DocumentProcessingInfo";

}
