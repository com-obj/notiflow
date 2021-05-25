package com.obj.nc.functions.processors.senders.mailchimp.dtos;

import lombok.Data;

@Data
public class MailchimpAttachmentDto {

    private String type;
    private String name;
    private String content;

}
