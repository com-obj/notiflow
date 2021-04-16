package com.obj.nc.domain.endpoints;

import lombok.*;

import javax.validation.constraints.NotNull;

@Data
@EqualsAndHashCode(callSuper = false, of = "phone")
@NoArgsConstructor
@AllArgsConstructor
public class SmsEndpoint extends RecievingEndpoint {

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
    public String getEndpointType() {
        return JSON_TYPE_IDENTIFIER;
    }

}
