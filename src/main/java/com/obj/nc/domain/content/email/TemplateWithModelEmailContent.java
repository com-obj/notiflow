package com.obj.nc.domain.content.email;

import static com.obj.nc.functions.processors.messageTemplating.config.ThymeleafConfiguration.MESSAGE_SOURCE_FOR_TEMPLATES_BEAN_NAME;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.obj.nc.domain.Attachment;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;

import com.obj.nc.Get;
import com.obj.nc.domain.content.TemplateWithModelContent;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Log4j2
@Builder
@AllArgsConstructor
public class TemplateWithModelEmailContent<MODEL_TYPE> extends TemplateWithModelContent<MODEL_TYPE>  {
	
	private String subjectResourceKey;
	
	private String[] subjectResourcesMessageParameters;
	
	private String subject;

	@Builder.Default
	private List<Attachment> attachments = new ArrayList<Attachment>();
		
	public String getSubjectLocalised(Locale locale) {		
		try {
			return Get.getBean(MESSAGE_SOURCE_FOR_TEMPLATES_BEAN_NAME, MessageSource.class).getMessage(getSubjectResourceKey(), getSubjectResourcesMessageParameters(), locale);
		} catch (NoSuchMessageException e) {
			log.debug("{} not found in resource bundle. Fallback to subject property", getSubjectResourceKey());
		}
		return subject;
	}
	
}
