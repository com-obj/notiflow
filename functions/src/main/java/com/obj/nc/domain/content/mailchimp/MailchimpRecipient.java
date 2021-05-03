package com.obj.nc.domain.content.mailchimp;

import com.fasterxml.jackson.annotation.JsonValue;

import lombok.Data;

@Data
public class MailchimpRecipient {

    private String email;
    private String name;
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
