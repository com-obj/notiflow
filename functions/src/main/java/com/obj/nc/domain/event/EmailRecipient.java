package com.obj.nc.domain.event;

import javax.validation.constraints.NotNull;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=true)
public class EmailRecipient extends Recipient {

	@NotNull
	String email;
	
	public static EmailRecipient create(String name, String emailAddress) {
		EmailRecipient r = new EmailRecipient();
		r.setName(name);
		r.setEmail(emailAddress);
		return r;
	}


	
}
