package com.obj.nc.domain.endpoints;

import javax.validation.constraints.NotNull;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
public class EmailEndpoint extends RecievingEndpoint {

	@NotNull
	private String email;
	
	private Recipient recipient;
	
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


	
}
