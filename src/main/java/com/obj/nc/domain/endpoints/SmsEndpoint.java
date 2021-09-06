package com.obj.nc.domain.endpoints;

import javax.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = false, of = "phone")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SmsEndpoint extends ReceivingEndpoint {

    public static final String JSON_TYPE_IDENTIFIER = "SMS";

    @NotNull
    private String phone;

    public static SmsEndpoint createForPerson(Person person, String phone) {
        SmsEndpoint smsEndpoint = new SmsEndpoint(phone);
        smsEndpoint.setRecipient(person);
        return smsEndpoint;
    }

    @Override
    public String getEndpointId() {
        return phone;
    }
    
	@Override
	public void setEndpointId(String endpointId) {
		this.phone = endpointId;
	}

    @Override
    public String getEndpointType() {
        return JSON_TYPE_IDENTIFIER;
    }

}
