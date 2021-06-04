package com.obj.nc.domain.content.email;

import static com.obj.nc.functions.processors.messageTemplating.config.ThymeleafConfiguration.MESSAGE_SOURCE_FOR_TEMPLATES_BEAN_NAME;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;

import com.obj.nc.Get;
import com.obj.nc.domain.Attachement;
import com.obj.nc.domain.content.TemplateWithModelContent;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;

@Data
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Log4j2
@Builder
@AllArgsConstructor
public class TemplateWithModelEmailContent<MODEL_TYPE> extends TemplateWithModelContent<MODEL_TYPE>  {
	
	public final static String JSON_TYPE_IDENTIFIER = "EMAIL_FROM_TEAMPLATE_POJO_CONTENT";
	
	@NonNull
	@EqualsAndHashCode.Include
	private String subjectResourceKey;
	
	@NonNull
	@EqualsAndHashCode.Include
	private String[] subjectResourcesMessageParameters;
	
	@NonNull
	@EqualsAndHashCode.Include
	private String subject;

	@EqualsAndHashCode.Include
	@Builder.Default
	private List<Attachement> attachments = new ArrayList<Attachement>();
		
	public String getSubjectLocalised(Locale locale) {
		try {
			return Get.getBean(MESSAGE_SOURCE_FOR_TEMPLATES_BEAN_NAME, MessageSource.class).getMessage(getSubjectResourceKey(), getSubjectResourcesMessageParameters(), locale);
		} catch (NoSuchMessageException e) {
			log.debug("{} not found in resource bundle. Fallback to subject property", getSubjectResourceKey());
		}
		return subject;
	}

	@Override
	public String getContentTypeName() {
		return JSON_TYPE_IDENTIFIER;
	}
	
}
