package com.obj.nc.functions.processors.senders.mailchimp;

import com.obj.nc.domain.content.mailchimp.MailchimpData;
import com.obj.nc.domain.content.mailchimp.MailchimpMergeVariable;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class MailchimpMergeVarMapperImpl implements MailchimpMergeVarMapper {
    
    @Override
    public List<MailchimpMergeVariable> map(MailchimpData data) {
        return Arrays.asList(new MailchimpMergeVariable(data.getType(), data.getData()));
    }
    
}
