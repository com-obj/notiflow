package com.obj.nc.domain.endpoints;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@EqualsAndHashCode(callSuper=false)
@NoArgsConstructor
//could be potentially EmailEndpoint but endpoint type is important for intent->message translation and correct message subtyping
public class MailchimpEndpoint extends RecievingEndpoint {
	
	public static final String JSON_TYPE_IDENTIFIER = "MAILCHIMP";
	
	@NonNull
	private String email;
	
	public static MailchimpEndpoint createForPerson(String email, Person person) {
		MailchimpEndpoint mailChimpEndpoint = new MailchimpEndpoint();
		mailChimpEndpoint.email = email;
		mailChimpEndpoint.setRecipient(person);
		return mailChimpEndpoint;
	}
	
	@Override
	public String getEndpointId() {
		return email;
	}
	
	@Override
	public void setEndpointId(String endpointId) {
		this.email = endpointId;
	}
	
	@Override
	public String getEndpointType() {
		return JSON_TYPE_IDENTIFIER;
	}
	
}
