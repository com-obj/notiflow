package com.obj.nc.dto.mailchimp;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class RecipientDto {

    private String email;

    private String name;

    private Type type = Type.TO;

    public enum Type {
        TO("to"), CC("cc"), BCC("bcc");

        private String value;

        Type(String value) {
            this.value = value;
        }

        @JsonValue
        public String getValue() {
            return value;
        }
    }

}
