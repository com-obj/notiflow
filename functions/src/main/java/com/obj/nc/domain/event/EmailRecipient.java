package com.obj.nc.domain.event;

public class EmailRecipient extends Recipient {

	String email;
	
	public static EmailRecipient create(String name, String emailAddress) {
		EmailRecipient r = new EmailRecipient();
		r.setName(name);
		r.setEmail(emailAddress);
		return r;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}
	
	
}
