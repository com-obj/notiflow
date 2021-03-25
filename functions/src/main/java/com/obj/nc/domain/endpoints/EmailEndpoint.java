package com.obj.nc.domain.endpoints;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Data
@EqualsAndHashCode(callSuper=false, of = "email")
@RequiredArgsConstructor
@NoArgsConstructor
public class EmailEndpoint extends RecievingEndpoint {
	
	public static final String JSON_TYPE_IDENTIFIER = "EMAIL";

	@NonNull
	private String email;
	
	public static EmailEndpoint createForPerson(Person person, String emailAddress) {
		EmailEndpoint r = new EmailEndpoint(emailAddress);
		r.setRecipient(person);
		return r;
	}
	
	public static EmailEndpoint createForGroup(Group group, String emailAddress) {
		EmailEndpoint r = new EmailEndpoint(emailAddress);
		r.setRecipient(group);
		return r;
	}

	@Override
	public String getEndpointId() {
		return email;
	}

	@Override
	public String getEndpointTypeName() {
		return JSON_TYPE_IDENTIFIER;
	}
	
}
