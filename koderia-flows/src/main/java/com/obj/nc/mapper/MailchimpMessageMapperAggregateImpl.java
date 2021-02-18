package com.obj.nc.mapper;

import com.obj.nc.domain.message.Message;
import com.obj.nc.domain.message.MessageContent;
import com.obj.nc.dto.EmitEventDto;
import com.obj.nc.dto.mailchimp.*;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

import static com.obj.nc.functions.processors.KoderiaEventConverterExecution.ORIGINAL_EVENT_FIELD;
import static com.obj.nc.mapper.MailchimpMessageMapperAggregateImpl.COMPONENT_NAME;

@Component(COMPONENT_NAME)
public class MailchimpMessageMapperAggregateImpl extends MailchimpMessageMapperImpl {

    public static final String COMPONENT_NAME = "mailchimpAggregateMessageMapper";

    @Override
    protected String mapSubject(Message message) {
        return mailchimpApiConfig.getTemplate().getAggregateSubject();
    }

    @Override
    protected List<MergeVarDto> mapGlobalMergeVars(Message message) {
        List<MessageContent> aggregateContent = message.getBody().getMessage().getAggregateContent();

        Map<String, List<Object>> globalMergeCategoryValues = new HashMap<>();
        Arrays.stream(EmitEventDto.Type.values())
                .forEach(type -> globalMergeCategoryValues.put(type.name(), new ArrayList<>()));

        aggregateContent.stream()
                .map(messageContent -> messageContent.getAttributeValueAs(ORIGINAL_EVENT_FIELD, EmitEventDto.class))
                .forEach(originalEvent -> globalMergeCategoryValues.get(originalEvent.getType().name()).add(originalEvent.asMap()));

        return globalMergeCategoryValues.entrySet().stream().map(this::mapMergeVar).collect(Collectors.toList());
    }

    @Override
    protected List<AttachmentDto> mapAttachments(Message message) {
        List<MessageContent> aggregateContent = message.getBody().getMessage().getAggregateContent();
        return aggregateContent.stream()
                .flatMap(messageContent -> messageContent.getAttachments().stream().map(this::mapAttachment))
                .collect(Collectors.toList());
    }

    @Override
    protected String getTemplateName(Message message) {
        return mailchimpApiConfig.getTemplate().getAggregateName();
    }

}
