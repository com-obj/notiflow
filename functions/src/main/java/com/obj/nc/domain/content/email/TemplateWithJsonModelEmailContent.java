package com.obj.nc.domain.content.email;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.obj.nc.domain.BaseJSONObject;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@JsonTypeName(TemplateWithJsonModelEmailContent.JSON_TYPE_IDENTIFIER)
public class TemplateWithJsonModelEmailContent extends TemplateWithModelEmailContent<BaseJSONObject> implements Cloneable {
	
	public final static String JSON_TYPE_IDENTIFIER = "EMAIL_FROM_TEAMPLATE_MESSAGE_CONTENT";

}
