package com.obj.nc.domain.content;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;

import com.obj.nc.Get;
import com.obj.nc.domain.Attachement;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Data
@NoArgsConstructor
@RequiredArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Log4j2
public abstract class TemplateWithModelBasedContent<MODEL_TYPE> extends Content {

	@NonNull
	@EqualsAndHashCode.Include
	private String subject;
	
	@NonNull
	@EqualsAndHashCode.Include
	private String subjectResourceKey;
	
	@NonNull
	@EqualsAndHashCode.Include
	private String[] subjectResourcesMessageParameters;
	
	@NonNull
	@EqualsAndHashCode.Include
	private String templateFileName;
	
	@NonNull
	@EqualsAndHashCode.Include
	private MODEL_TYPE model;
	
	private List<Locale> requiredLocales = new ArrayList<>();

	@EqualsAndHashCode.Include
	private List<Attachement> attachments = new ArrayList<Attachement>();
	
	public String getSubjectLocalised(Locale locale) {
		try {
			return Get.getBean("nc.emailTemplateFormatter.messageSource", MessageSource.class).getMessage(subjectResourceKey, subjectResourcesMessageParameters, locale);
		} catch (NoSuchMessageException e) {
			log.debug("{} not found in resource bundle. Fallback to subject property", subjectResourceKey);
		}
		return subject;
	}

}
