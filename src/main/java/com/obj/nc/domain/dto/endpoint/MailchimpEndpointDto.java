package com.obj.nc.domain.dto.endpoint;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false, of = "email")
@JsonTypeName("MAILCHIMP")
public class MailchimpEndpointDto extends ReceivingEndpointDto {

    public static final String JSON_TYPE_IDENTIFIER = "MAILCHIMP";

    private String email;

    public static MailchimpEndpointDto create(String id, String email) {
        MailchimpEndpointDto dto = new MailchimpEndpointDto();
        dto.setId(id);
        dto.setEmail(email);
        return dto;
    }

    @Override
    public String getValue() {
        return this.email;
    }

    @Override
    public void setValue(String email) {
        this.email = email;
    }
}
