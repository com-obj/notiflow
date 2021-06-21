package com.obj.nc.aspects;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface DocumentProcessingInfo {
	
	String value() default "SET_STEP_NAME_TO_DocumentProcessingInfo";

}
