package com.obj.nc.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.obj.nc.domain.message.Message;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;

@Data
@ToString(callSuper = false)
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
public class Messages extends BasePayload {

    public static final String JSON_TYPE_IDENTIFIER = "MESSAGES";

    private List<Message> messages;

    @Override
    @JsonIgnore
    public String getPayloadTypeName() {
        return JSON_TYPE_IDENTIFIER;
    }
}
