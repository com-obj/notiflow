package com.obj.nc.domain.message;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.obj.nc.domain.BasePayload;
import com.obj.nc.domain.content.email.EmailContent;
import com.obj.nc.domain.content.sms.SimpleTextContent;
import com.obj.nc.domain.endpoints.EmailEndpoint;
import com.obj.nc.domain.endpoints.SmsEndpoint;
import com.obj.nc.domain.headers.HasHeader;
import com.obj.nc.exceptions.PayloadValidationException;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Table;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
@ToString(callSuper = false)
@Table("nc_message")
public class Message extends BasePayload implements HasHeader {
	
	public static final String JSON_TYPE_IDENTIFIER = "MESSAGE";
	
	public static Message createAsEmail() {
		Message msg = new Message();
		msg.getBody().setMessage(new EmailContent());
		return msg;
	}
	
	//TODO: refactor as some factory or something else
	public static Message createAsSms() {
		Message msg = new Message();
		msg.getBody().setMessage(new SimpleTextContent());
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
		if (this.getBody().getRecievingEndpoints().size()!=1) {
			throw new PayloadValidationException("Message should have only single endpoint");
		}
		
		return this.getBody().getRecievingEndpoints().iterator().next() instanceof EmailEndpoint;
	}
	
	@JsonIgnore
	@Transient
	public boolean isSmsMessage() {
		if (this.getBody().getRecievingEndpoints().size()!=1) {
			throw new PayloadValidationException("Message should have only single endpoint");
		}
		
		return this.getBody().getRecievingEndpoints().iterator().next() instanceof SmsEndpoint;
	}

}
