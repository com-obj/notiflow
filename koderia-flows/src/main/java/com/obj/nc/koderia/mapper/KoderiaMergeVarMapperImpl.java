package com.obj.nc.koderia.mapper;

import com.obj.nc.domain.content.mailchimp.MailchimpData;
import com.obj.nc.functions.processors.senders.mailchimp.MailchimpMergeVarMapper;
import com.obj.nc.functions.processors.senders.mailchimp.dtos.MailchimpMergeVariableDto;
import com.obj.nc.koderia.domain.event.BaseKoderiaEvent;
import com.obj.nc.koderia.domain.eventData.BaseKoderiaData;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Primary
@Component
public class KoderiaMergeVarMapperImpl implements MailchimpMergeVarMapper {
    
    @Override
    public List<MailchimpMergeVariableDto> map(MailchimpData data) {
        MailchimpMergeVariableDto mergeVar = new MailchimpMergeVariableDto();
        mergeVar.setName(data.getType());
        mergeVar.setContent(mapVariableContent((BaseKoderiaEvent) data));
        
        List<MailchimpMergeVariableDto> result = new ArrayList<>();
        result.add(mergeVar);
        return result;
    }
    
    private Object mapVariableContent(BaseKoderiaEvent message) {
        BaseKoderiaData data = message.getData();
        return data.asMailchimpMergeVarContent();
    }
    
}
