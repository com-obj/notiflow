package com.obj.nc.koderia.mapper;

import com.obj.nc.domain.content.email.AggregatedEmailContent;
import com.obj.nc.domain.message.Message;
import com.obj.nc.flows.testmode.mailchimp.dto.AttachmentDto;
import com.obj.nc.flows.testmode.mailchimp.dto.MergeVarDto;
import com.obj.nc.koderia.dto.koderia.event.BaseKoderiaEventDto;

import org.springframework.stereotype.Component;

import static com.obj.nc.functions.processors.eventFactory.GenericEventToNotificaitonIntentConverter.ORIGINAL_EVENT_FIELD;
import static com.obj.nc.koderia.mapper.MailchimpMessageMapperAggregateImpl.COMPONENT_NAME;

import java.util.*;
import java.util.stream.Collectors;

@Component(COMPONENT_NAME)
public class MailchimpMessageMapperAggregateImpl extends MailchimpMessageMapperImpl {

    public static final String COMPONENT_NAME = "mailchimpAggregateMessageMapper";

    @Override
    protected List<MergeVarDto> mapGlobalMergeVars(Message message) {
    	AggregatedEmailContent aggregateContent = (AggregatedEmailContent)message.getBody().getMessage();

        Map<String, List<Object>> globalMergeCategoryValues = new HashMap<>();
        Arrays.stream(BaseKoderiaEventDto.Type.values())
                .forEach(type -> globalMergeCategoryValues.put(type.name(), new ArrayList<>()));

        aggregateContent.getAggregateContent().stream()
                .map(messageContent -> messageContent.getAttributeValueAs(ORIGINAL_EVENT_FIELD, BaseKoderiaEventDto.class))
                .forEach(originalEvent -> globalMergeCategoryValues.get(originalEvent.getTypeName()).add(originalEvent.asMap()));

        return globalMergeCategoryValues.entrySet().stream().map(this::mapMergeVar).collect(Collectors.toList());
    }
    
    @Override
    protected String mapSubject(Message message) {
        return mailchimpSenderConfig.getAggregateSubject();
    }

    @Override
    protected List<AttachmentDto> mapAttachments(Message message) {
    	AggregatedEmailContent aggregateContent = (AggregatedEmailContent)message.getBody().getMessage();
    	
        return aggregateContent.getAggregateContent().stream()
                .flatMap(messageContent -> messageContent.getAttachments().stream().map(this::mapAttachment))
                .collect(Collectors.toList());
    }

    @Override
    protected String getTemplateName(Message message) {
        return mailchimpSenderConfig.getMailchimpTemplateNames().getAggregate();
    }

}
