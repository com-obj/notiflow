package com.obj.nc.domain.message;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.obj.nc.domain.Attachement;
import com.obj.nc.domain.BaseJSONObject;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Data
@NoArgsConstructor
@RequiredArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public abstract class BaseEmailFromTemplate<MODEL_TYPE> extends Content {

	@NonNull
	@EqualsAndHashCode.Include
	private String subject;
	
	@NonNull
	@EqualsAndHashCode.Include
	private String templateFileName;
	
	@NonNull
	@EqualsAndHashCode.Include
	private MODEL_TYPE model;
	
	private List<Locale> requiredLocales = new ArrayList<>();

	@EqualsAndHashCode.Include
	private List<Attachement> attachments = new ArrayList<Attachement>();

}
