package com.obj.nc.functions.processors.senders.mailchimp.dtos;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MailchimpTemplateContentDto {

    private String name;
    private String content;

}
