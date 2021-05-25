package com.obj.nc.functions.processors.senders.mailchimp;

import com.obj.nc.domain.content.mailchimp.MailchimpData;
import com.obj.nc.functions.processors.senders.mailchimp.dtos.MailchimpMergeVariableDto;

import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class MailchimpMergeVarMapperImpl implements MailchimpMergeVarMapper {
    
    @Override
    public List<MailchimpMergeVariableDto> map(MailchimpData data) {
        return Arrays.asList(new MailchimpMergeVariableDto(data.getType(), data.getData()));
    }
    
}
