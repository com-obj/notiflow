package com.obj.nc.domain.endpoints;

import java.util.List;

public interface Recipient {
	
	public String getName();
	
	public abstract List<Person> getFinalRecipientsAsPersons();


}
