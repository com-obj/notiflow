package com.obj.nc.domain.endpoints;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Data
@EqualsAndHashCode(callSuper=false, of = "email")
@RequiredArgsConstructor
@NoArgsConstructor
@Builder
public class EmailEndpoint extends RecievingEndpoint {
	
	public static final String JSON_TYPE_IDENTIFIER = "EMAIL";

	@NonNull
	private String email;
	
	public static EmailEndpoint createForPerson(Person person, String emailAddress) {
		EmailEndpoint r = EmailEndpoint.builder()
				.email(emailAddress)
				.build();
		
		r.setRecipient(person);
		return r;
	}
	
	public static EmailEndpoint createForGroup(Group group, String emailAddress) {
		EmailEndpoint r = EmailEndpoint.builder()
				.email(emailAddress)
				.build();
		
		r.setRecipient(group);
		return r;
	}

	@Override
	public String getEndpointId() {
		return email;
	}

	@Override
	public String getEndpointType() {
		return JSON_TYPE_IDENTIFIER;
	}
	
}
