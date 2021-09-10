package com.obj.nc.domain.endpoints;

import com.google.common.collect.ObjectArrays;

import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.PayloadDocumentation;

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
public class EmailEndpoint extends ReceivingEndpoint {
	
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
	public void setEndpointId(String endpointId) {
		this.email = endpointId;
	}

	@Override
	public String getEndpointType() {
		return JSON_TYPE_IDENTIFIER;
	}

	public static FieldDescriptor[] fieldDesc = ObjectArrays.concat(
        ReceivingEndpoint.fieldDesc,
        new FieldDescriptor[] {
			PayloadDocumentation.fieldWithPath("email").description("Email address"),        
        },
        FieldDescriptor.class
    );

	
	
}
