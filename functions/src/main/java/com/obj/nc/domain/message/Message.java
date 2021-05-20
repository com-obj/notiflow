package com.obj.nc.domain.message;

import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.obj.nc.domain.BasePayload;
import com.obj.nc.domain.content.email.EmailContent;
import com.obj.nc.domain.content.sms.SimpleTextContent;
import com.obj.nc.domain.endpoints.EmailEndpoint;
import com.obj.nc.domain.endpoints.MailchimpEndpoint;
import com.obj.nc.domain.endpoints.SmsEndpoint;
import com.obj.nc.domain.headers.HasHeader;
import com.obj.nc.exceptions.PayloadValidationException;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
@ToString(callSuper = false)
@Table("nc_message")
public class Message<BODY_TYPE> extends BasePayload<BODY_TYPE> implements HasHeader {
	
	public static final String JSON_TYPE_IDENTIFIER = "MESSAGE";
	
	public static Message<EmailContent> createAsEmail() {
		Message<EmailContent> msg = new Message<EmailContent>();
		msg.setBody(new EmailContent());
		return msg;
	}
	
	//TODO: refactor as some factory or something else
	public static Message<SimpleTextContent> createAsSms() {
		Message<SimpleTextContent> msg = new Message<SimpleTextContent>();
		return msg;
	}

	@Override
	@JsonIgnore
	public String getPayloadTypeName() {
		return JSON_TYPE_IDENTIFIER;
	}

	@JsonIgnore
	@Transient
	public boolean isEmailMessage() {
		if (this.getRecievingEndpoints().size()!=1) {
			throw new PayloadValidationException("Message should have only single endpoint");
		}
		
		return this.getRecievingEndpoints().iterator().next() instanceof EmailEndpoint;
	}
	
	@JsonIgnore
	@Transient
	public boolean isSmsMessage() {
		if (this.getRecievingEndpoints().size()!=1) {
			throw new PayloadValidationException("Message should have only single endpoint");
		}
		
		return this.getRecievingEndpoints().iterator().next() instanceof SmsEndpoint;
	}
	
	@JsonIgnore
	@Transient
	public boolean isMailchimpMessage() {
		if (this.getRecievingEndpoints().size()!=1) {
			throw new PayloadValidationException("Message should have only single endpoint");
		}
		
		return this.getRecievingEndpoints().iterator().next() instanceof MailchimpEndpoint;
	}
}
