package com.obj.nc.domain.content.mailchimp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MailchimpMergeVariable {

    private String name;
    private Object content;

}
