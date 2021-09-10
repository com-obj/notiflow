package com.obj.nc.functions.processors.senders.mailchimp.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MailchimpMergeVariableDto {

    private String name;
    private Object content;

}
