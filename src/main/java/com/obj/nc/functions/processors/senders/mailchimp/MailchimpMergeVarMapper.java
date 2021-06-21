package com.obj.nc.functions.processors.senders.mailchimp;

import com.obj.nc.domain.content.mailchimp.MailchimpData;
import com.obj.nc.functions.processors.senders.mailchimp.dtos.MailchimpMergeVariableDto;

import java.util.List;

public interface MailchimpMergeVarMapper {
    
    List<MailchimpMergeVariableDto> map(MailchimpData data);
    
}
