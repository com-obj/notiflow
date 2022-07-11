package com.obj.nc.domain.dto.endpoint;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = EmailEndpointDto.class, name = EmailEndpointDto.JSON_TYPE_IDENTIFIER),
        @JsonSubTypes.Type(value = MailchimpEndpointDto.class, name = MailchimpEndpointDto.JSON_TYPE_IDENTIFIER),
        @JsonSubTypes.Type(value = SmsEndpointDto.class, name = SmsEndpointDto.JSON_TYPE_IDENTIFIER),
        @JsonSubTypes.Type(value = DirectPushEndpointDto.class, name = DirectPushEndpointDto.JSON_TYPE_IDENTIFIER),
        @JsonSubTypes.Type(value = TopicPushEndpointDto.class, name = TopicPushEndpointDto.JSON_TYPE_IDENTIFIER)
})
@Data
public abstract class ReceivingEndpointDto {
    private String id;

    public abstract String getValue();
    public abstract void setValue(String value);
}
