package com.obj.nc.functions.processors.senders.mailchimp;

import com.obj.nc.domain.content.mailchimp.MailchimpData;
import com.obj.nc.domain.content.mailchimp.MailchimpMergeVariable;

import java.util.List;

public interface MailchimpMergeVarMapper {
    
    List<MailchimpMergeVariable> map(MailchimpData data);
    
}
