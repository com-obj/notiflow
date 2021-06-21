package com.obj.nc.domain.content;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Data
@NoArgsConstructor
@RequiredArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class TemplateWithModelContent<MODEL_TYPE> extends MessageContent {
	
	@NonNull
	private String templateFileName;
	
	@JsonTypeInfo(use = Id.CLASS)
	private MODEL_TYPE model;
	
	private List<Locale> requiredLocales = new ArrayList<>();
	
}
