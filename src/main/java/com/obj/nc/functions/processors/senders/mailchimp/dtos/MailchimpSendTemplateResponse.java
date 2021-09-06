package com.obj.nc.functions.processors.senders.mailchimp.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class MailchimpSendTemplateResponse {

    private String email;
    private String status;
    @JsonProperty("reject_reason") private String rejectReason;
    @JsonProperty("_id") private String id;

}
