package com.obj.nc.domain.content;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.obj.nc.domain.BaseJSONObject;

import lombok.Data;
import lombok.EqualsAndHashCode;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type", defaultImpl = EmailContent.class)
@JsonSubTypes({ 
	@Type(value = EmailContent.class, name = EmailContent.JSON_TYPE_IDENTIFIER),
	@Type(value = AggregatedEmail.class, name = AggregatedEmail.JSON_TYPE_IDENTIFIER),
	@Type(value = SimpleText.class, name = SimpleText.JSON_TYPE_IDENTIFIER),
	@Type(value = EmailFromTemplateJsonModel.class, name = EmailFromTemplateJsonModel.JSON_TYPE_IDENTIFIER),
})
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public abstract class Content extends BaseJSONObject {
	
}
