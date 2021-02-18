package com.obj.nc.mapper;

import com.obj.nc.domain.message.Message;
import com.obj.nc.domain.message.MessageContent;
import com.obj.nc.dto.EmitEventDto;
import com.obj.nc.dto.mailchimp.*;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.obj.nc.functions.processors.KoderiaEventConverterExecution.ORIGINAL_EVENT_FIELD;
import static com.obj.nc.mapper.MailchimpMessageMapperAggregateImpl.COMPONENT_NAME;

@Component(COMPONENT_NAME)
public class MailchimpMessageMapperAggregateImpl extends MailchimpMessageMapperImpl {

    public static final String COMPONENT_NAME = "mailchimpAggregateMessageMapper";

    @Override
    protected MessageDto mapMessage(Message message) {
        MessageDto messageDto = new MessageDto();
        messageDto.setSubject(mailchimpApiConfig.getTemplate().getAggregateSubject());
        messageDto.setFrom_email(mailchimpApiConfig.getApi().getFromEmail());
        messageDto.setFrom_name(mailchimpApiConfig.getApi().getFromName());

        List<RecipientDto> recipients = Collections.singletonList(this.mapRecipient(message.getBody().getRecievingEndpoints().get(0)));
        messageDto.setTo(recipients);

        List<MessageContent> aggregateContent = message.getBody().getMessage().getAggregateContent();

        List<MergeVarDto> globalMergeVars = aggregateContent.stream()
                .flatMap(messageContent -> {
                    EmitEventDto originalEvent = messageContent.getAttributeValueAs(ORIGINAL_EVENT_FIELD, EmitEventDto.class);
                    return originalEvent.asMap().entrySet().stream().map(this::mapFieldValue);
                })
                .collect(Collectors.toList());

        messageDto.setGlobal_merge_vars(globalMergeVars);

        List<AttachmentDto> attachments = aggregateContent.stream()
                .flatMap(messageContent -> messageContent.getAttachments().stream().map(this::mapAttachment))
                .collect(Collectors.toList());

        messageDto.setAttachments(attachments);
        return messageDto;
    }

    @Override
    protected String getTemplateName(Message message) {
        return mailchimpApiConfig.getTemplate().getAggregateName();
    }

}
