package com.obj.nc.functions.processors.senders.mailchimp.dtos;

import com.fasterxml.jackson.annotation.JsonValue;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MailchimpRecipientDto {

    private String email;
    private String name;
    @Builder.Default
    private Type type = Type.TO;

    public enum Type {
        TO("to"), CC("cc"), BCC("bcc");

        private final String value;

        Type(String value) {
            this.value = value;
        }

        @JsonValue
        public String getValue() {
            return value;
        }
    }

}
