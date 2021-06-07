package com.obj.nc.domain.content;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.fasterxml.jackson.annotation.JsonTypeName;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Data
@NoArgsConstructor
@RequiredArgsConstructor
@EqualsAndHashCode(callSuper = false)
@JsonTypeName(TemplateWithModelContent.JSON_TYPE_IDENTIFIER)
public class TemplateWithModelContent<MODEL_TYPE> extends MessageContent {
	
	public final static String JSON_TYPE_IDENTIFIER = "TEMPLATE_WITH_MODEL_CONTENT";
	
	@NonNull
	private String templateFileName;
	
	@JsonTypeInfo(use = Id.CLASS)
	private MODEL_TYPE model;
	
	private List<Locale> requiredLocales = new ArrayList<>();
	
    @Override
    public String getContentTypeName() {
    	return JSON_TYPE_IDENTIFIER;
    }
	
}
