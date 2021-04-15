package com.obj.nc.flows.testmode.mailchimp.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class MessageResponseDto {

    private String email;

    private String status;

    @JsonProperty("reject_reason")
    private String rejectReason;

    @JsonProperty("_id")
    private String id;

}
