package com.obj.nc.domain.content;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.obj.nc.domain.BaseJSONObject;
import com.obj.nc.domain.content.email.AggregatedEmailContent;
import com.obj.nc.domain.content.email.EmailContent;
import com.obj.nc.domain.content.email.TemplateWithJsonModelEmailContent;

import lombok.Data;
import lombok.EqualsAndHashCode;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type", defaultImpl = EmailContent.class)
@JsonSubTypes({ 
	@Type(value = EmailContent.class, name = EmailContent.JSON_TYPE_IDENTIFIER),
	@Type(value = AggregatedEmailContent.class, name = AggregatedEmailContent.JSON_TYPE_IDENTIFIER),
	@Type(value = SimpleTextContent.class, name = SimpleTextContent.JSON_TYPE_IDENTIFIER),
	@Type(value = TemplateWithJsonModelEmailContent.class, name = TemplateWithJsonModelEmailContent.JSON_TYPE_IDENTIFIER),
})
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public abstract class Content extends BaseJSONObject {
	
}
