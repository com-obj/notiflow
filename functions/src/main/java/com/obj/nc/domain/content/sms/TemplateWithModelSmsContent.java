package com.obj.nc.domain.content.sms;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.obj.nc.domain.content.TemplateWithModelContent;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@JsonTypeName(TemplateWithModelSmsContent.JSON_TYPE_IDENTIFIER)
@NoArgsConstructor
public class TemplateWithModelSmsContent<MODEL_TYPE> extends TemplateWithModelContent<MODEL_TYPE> {
	
	public final static String JSON_TYPE_IDENTIFIER = "SMS_FROM_TEMPLATE_POJO_CONTENT";

    @Override
    public String getContentTypeName() {
    	return JSON_TYPE_IDENTIFIER;
    }
}
