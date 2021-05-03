package com.obj.nc.functions.processors.senders.mailchimp.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class MailchimpResponseDto {

    private String email;
    private String status;
    @JsonProperty("reject_reason") private String rejectReason;
    @JsonProperty("_id") private String id;

}
