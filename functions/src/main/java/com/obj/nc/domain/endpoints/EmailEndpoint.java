package com.obj.nc.domain.endpoints;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper=false)
@NoArgsConstructor
public class EmailEndpoint extends RecievingEndpoint {
	
	public static final String JSON_TYPE_IDENTIFIER = "EMAIL";

	@NotNull
	private String email;
	
	public static EmailEndpoint createForPerson(Person person, String emailAddress) {
		EmailEndpoint r = new EmailEndpoint();
		r.setRecipient(person);
		r.setEmail(emailAddress);
		return r;
	}
	
	public static EmailEndpoint createForGroup(Group group, String emailAddress) {
		EmailEndpoint r = new EmailEndpoint();
		r.setRecipient(group);
		r.setEmail(emailAddress);
		return r;
	}

	@Override
	@JsonIgnore
	public String getEndpointId() {
		return email;
	}

	@Override
	public String getEndpointTypeName() {
		return JSON_TYPE_IDENTIFIER;
	}
	
}
