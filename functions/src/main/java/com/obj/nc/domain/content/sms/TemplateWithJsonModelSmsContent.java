package com.obj.nc.domain.content.sms;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.obj.nc.domain.content.TemplateWithModelContent;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@JsonTypeName(TemplateWithJsonModelSmsContent.JSON_TYPE_IDENTIFIER)
public class TemplateWithJsonModelSmsContent<MODEL_TYPE> extends TemplateWithModelContent<MODEL_TYPE> {
	
	public final static String JSON_TYPE_IDENTIFIER = "SMS_FROM_TEMPLATE_MESSAGE_CONTENT";

}
