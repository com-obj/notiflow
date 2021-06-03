package com.obj.nc.domain.content;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.obj.nc.domain.BaseJSONObject;
import com.obj.nc.domain.content.email.EmailContent;
import com.obj.nc.domain.content.email.TemplateWithJsonModelEmailContent;
import com.obj.nc.domain.content.email.TemplateWithModelEmailContent;
import com.obj.nc.domain.content.mailchimp.MailchimpContent;
import com.obj.nc.domain.content.sms.SimpleTextContent;
import com.obj.nc.domain.content.sms.TemplateWithJsonModelSmsContent;
import com.obj.nc.domain.notifIntent.content.ConstantIntentContent;
import com.obj.nc.domain.notifIntent.content.TemplatedIntentContent;

import lombok.Data;
import lombok.EqualsAndHashCode;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type", defaultImpl = EmailContent.class)
@JsonSubTypes({ 
	@Type(value = EmailContent.class, name = EmailContent.JSON_TYPE_IDENTIFIER),
	@Type(value = SimpleTextContent.class, name = SimpleTextContent.JSON_TYPE_IDENTIFIER),
	@Type(value = MailchimpContent.class, name = MailchimpContent.JSON_TYPE_IDENTIFIER),
	@Type(value = TemplateWithModelEmailContent.class, name = TemplateWithModelEmailContent.JSON_TYPE_IDENTIFIER),
	@Type(value = TemplateWithJsonModelEmailContent.class, name = TemplateWithJsonModelEmailContent.JSON_TYPE_IDENTIFIER),
	@Type(value = TemplateWithJsonModelSmsContent.class, name = TemplateWithJsonModelSmsContent.JSON_TYPE_IDENTIFIER),
	
	@Type(value = ConstantIntentContent.class, name = ConstantIntentContent.JSON_TYPE_IDENTIFIER),
	@Type(value = TemplatedIntentContent.class, name = TemplatedIntentContent.JSON_TYPE_IDENTIFIER),
})
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public abstract class MessageContent extends BaseJSONObject {
	
	@JsonIgnore
	public abstract String getContentTypeName();
}
