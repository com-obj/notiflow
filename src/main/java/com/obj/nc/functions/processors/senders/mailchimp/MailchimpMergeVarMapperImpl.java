package com.obj.nc.functions.processors.senders.mailchimp;

import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Component;

import com.obj.nc.domain.content.mailchimp.MailchimpData;
import com.obj.nc.functions.processors.senders.mailchimp.dtos.MailchimpMergeVariableDto;

@Component
public class MailchimpMergeVarMapperImpl implements MailchimpMergeVarMapper {
    
    @Override
    public List<MailchimpMergeVariableDto> map(MailchimpData data) {
        return Arrays.asList(new MailchimpMergeVariableDto(data.getType(), data.getData()));
    }
    
}
