package com.obj.nc.functions.processors.senders.mailchimp;

import java.util.List;

import com.obj.nc.domain.content.mailchimp.MailchimpData;
import com.obj.nc.functions.processors.senders.mailchimp.dtos.MailchimpMergeVariableDto;

public interface MailchimpMergeVarMapper {
    
    List<MailchimpMergeVariableDto> map(MailchimpData data);
    
}
