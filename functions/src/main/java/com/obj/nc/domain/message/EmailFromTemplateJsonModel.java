package com.obj.nc.domain.message;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.obj.nc.domain.BaseJSONObject;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@JsonTypeName(EmailFromTemplateJsonModel.JSON_TYPE_IDENTIFIER)
public class EmailFromTemplateJsonModel extends TemplateWithModelBasedContent<BaseJSONObject> {
	
	public final static String JSON_TYPE_IDENTIFIER = "EMAIL_FROM_TEAMPLATE_MESSAGE_CONTENT";

}
