package com.obj.nc.domain.content;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Data
@NoArgsConstructor
@RequiredArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public abstract class TemplateWithModelContent<MODEL_TYPE> extends MessageContent {
	
	@NonNull
	@EqualsAndHashCode.Include
	private String templateFileName;
	
	@NonNull
	@EqualsAndHashCode.Include
	private MODEL_TYPE model;
	
	private List<Locale> requiredLocales = new ArrayList<>();
	


}
