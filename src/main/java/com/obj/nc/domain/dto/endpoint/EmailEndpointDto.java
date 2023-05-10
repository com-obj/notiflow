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
@JsonTypeName("Email")
public class EmailEndpointDto extends ReceivingEndpointDto {

    public static final String JSON_TYPE_IDENTIFIER = "EMAIL";
    private String email;

    public static EmailEndpointDto create(String id, String email) {
        EmailEndpointDto dto = new EmailEndpointDto();
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
