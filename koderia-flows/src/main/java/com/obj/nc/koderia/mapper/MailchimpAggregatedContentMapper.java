package com.obj.nc.koderia.mapper;

import com.obj.nc.domain.content.email.AggregatedEmailContent;
import com.obj.nc.domain.content.mailchimp.MailchimpAttachment;
import com.obj.nc.domain.message.Message;
import com.obj.nc.domain.content.mailchimp.MailchimpMergeVariable;
import com.obj.nc.koderia.domain.event.BaseKoderiaEvent;

import org.springframework.stereotype.Component;

import static com.obj.nc.functions.processors.eventFactory.GenericEventToNotificaitonIntentConverter.ORIGINAL_EVENT_FIELD;

import java.util.*;
import java.util.stream.Collectors;

// TODO: change to aggregation strategy
public class MailchimpAggregatedContentMapper {

/*    @Override
    protected List<MailchimpMergeVariable> mapGlobalMergeVars(Message message) {
    	AggregatedEmailContent aggregateContent = (AggregatedEmailContent)message.getBody().getMessage();

        Map<String, List<Object>> globalMergeCategoryValues = new HashMap<>();
        Arrays.stream(BaseKoderiaEvent.Type.values())
                .forEach(type -> globalMergeCategoryValues.put(type.name(), new ArrayList<>()));

        aggregateContent.getAggregateContent().stream()
                .map(messageContent -> messageContent.getAttributeValueAs(ORIGINAL_EVENT_FIELD, BaseKoderiaEvent.class))
                .forEach(originalEvent -> globalMergeCategoryValues.get(originalEvent.getTypeName()).add(originalEvent.asMap()));

        return globalMergeCategoryValues.entrySet().stream().map(this::mapMergeVar).collect(Collectors.toList());
    }
    
    @Override
    protected String mapSubject(Message message) {
        return mailchimpSenderConfig.getAggregatedMessageSubject();
    }

    @Override
    protected List<MailchimpAttachment> mapAttachments(Message message) {
    	AggregatedEmailContent aggregateContent = (AggregatedEmailContent)message.getBody().getMessage();
    	
        return aggregateContent.getAggregateContent().stream()
                .flatMap(messageContent -> messageContent.getAttachments().stream().map(this::mapAttachment))
                .collect(Collectors.toList());
    }

    @Override
    protected String getTemplateName(Message message) {
        return mailchimpSenderConfig.getAggregatedMessageTemplateName();
    }*/

}
