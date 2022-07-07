package com.obj.nc.domain.dto.endpoint;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false, of = "webhookUrl")
@JsonTypeName("TEAMS")
public class TeamsEndpointDto extends ReceivingEndpointDto {

    public static final String JSON_TYPE_IDENTIFIER = "TEAMS";
    private String webhookUrl;

    public static TeamsEndpointDto create(String id, String webhookUrl) {
        TeamsEndpointDto dto = new TeamsEndpointDto();
        dto.setId(id);
        dto.setWebhookUrl(webhookUrl);
        return dto;
    }

    @Override
    public String getValue() {
        return this.webhookUrl;
    }

    @Override
    public void setValue(String webhookUrl) {
        this.webhookUrl = webhookUrl;
    }
}
