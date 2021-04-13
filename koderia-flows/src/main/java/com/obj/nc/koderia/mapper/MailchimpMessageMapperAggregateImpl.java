package com.obj.nc.koderia.mapper;

import com.obj.nc.domain.content.Content;
import com.obj.nc.domain.content.email.AggregatedEmailContent;
import com.obj.nc.domain.content.email.EmailContent;
import com.obj.nc.domain.message.Message;
import com.obj.nc.koderia.dto.EmitEventDto;
import com.obj.nc.koderia.dto.mailchimp.*;

import org.springframework.stereotype.Component;

import static com.obj.nc.koderia.functions.processors.KoderiaEventConverterExecution.ORIGINAL_EVENT_FIELD;
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
        Arrays.stream(EmitEventDto.Type.values())
                .forEach(type -> globalMergeCategoryValues.put(type.name(), new ArrayList<>()));

        aggregateContent.getAggregateContent().stream()
                .map(messageContent -> messageContent.getAttributeValueAs(ORIGINAL_EVENT_FIELD, EmitEventDto.class))
                .forEach(originalEvent -> globalMergeCategoryValues.get(originalEvent.getType().name()).add(originalEvent.asMap()));

        return globalMergeCategoryValues.entrySet().stream().map(this::mapMergeVar).collect(Collectors.toList());
    }
    
    @Override
    protected String mapSubject(Message message) {
        return mailchimpSenderConfigProperties.getAggregateSubject();
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
        return mailchimpSenderConfigProperties.getMailchimpTemplateNames().getAggregate();
    }

}
