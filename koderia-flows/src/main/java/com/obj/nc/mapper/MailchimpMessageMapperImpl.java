package com.obj.nc.mapper;

import com.obj.nc.config.MailchimpApiConfig;
import com.obj.nc.domain.Attachement;
import com.obj.nc.domain.endpoints.EmailEndpoint;
import com.obj.nc.domain.endpoints.RecievingEndpoint;
import com.obj.nc.domain.message.Message;
import com.obj.nc.domain.message.MessageContent;
import com.obj.nc.dto.EmitEventDto;
import com.obj.nc.dto.mailchimp.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import static com.obj.nc.functions.processors.KoderiaEventConverterExecution.ORIGINAL_EVENT_FIELD;

@Component
public class MailchimpMessageMapperImpl implements MailchimpMessageMapper {

    @Autowired
    private MailchimpApiConfig mailchimpApiConfig;

    @Override
    public SendMessageDto map(Message message) {
        return null;
    }

    @Override
    public SendMessageWithTemplateDto mapWithTemplate(Message message) {
        SendMessageWithTemplateDto dto = new SendMessageWithTemplateDto();
        dto.setKey(mailchimpApiConfig.getKey());
        dto.setMessage(mapMessage(message));

        MessageContent messageContent = message.getBody().getMessage().getContent();
        EmitEventDto originalEvent = messageContent.getAttributeValueAs(ORIGINAL_EVENT_FIELD, EmitEventDto.class);
        String messageType = originalEvent.getType().toString();
        dto.setTemplate_name(messageType);
        return dto;
    }

    private MessageDto mapMessage(Message message) {
        MessageDto messageDto = new MessageDto();

        MessageContent messageContent = message.getBody().getMessage().getContent();
        messageDto.setSubject(messageContent.getSubject());

        messageDto.setFrom_email(mailchimpApiConfig.getFromEmail());
        messageDto.setFrom_name(mailchimpApiConfig.getFromName());

        List<RecipientDto> recipients = Collections.singletonList(this.mapRecipient(message.getBody().getRecievingEndpoints().get(0)));
        messageDto.setTo(recipients);

        EmitEventDto originalEvent = messageContent.getAttributeValueAs(ORIGINAL_EVENT_FIELD, EmitEventDto.class);
        List<MergeVarDto> globalMergeVars = originalEvent.asMap().entrySet().stream()
                .map(this::mapFieldValue)
                .collect(Collectors.toList());

        messageDto.setGlobal_merge_vars(globalMergeVars);

        List<AttachmentDto> attachments = messageContent.getAttachments().stream()
                .map(this::mapAttachment)
                .collect(Collectors.toList());

        messageDto.setAttachments(attachments);
        return messageDto;
    }

    private RecipientDto mapRecipient(RecievingEndpoint endpoint) {
        if (!EmailEndpoint.JSON_TYPE_IDENTIFIER.equals(endpoint.getEndpointTypeName())) {
            throw new UnsupportedOperationException("Mapper can only map Email endpoint");
        }

        RecipientDto dto = new RecipientDto();
        dto.setName(endpoint.getRecipient().getName());
        dto.setEmail(((EmailEndpoint) endpoint).getEmail());
        return dto;
    }

    private MergeVarDto mapFieldValue(Map.Entry<String, Object> fieldValue) {
        MergeVarDto dto = new MergeVarDto();
        dto.setName(fieldValue.getKey());
        dto.setContent(fieldValue.getValue());
        return dto;
    }

    private AttachmentDto mapAttachment(Attachement attachement) {
        AttachmentDto dto = new AttachmentDto();
        dto.setName(attachement.getName());

        File file = new File(attachement.getFileURI());
        FileNameMap fileNameMap = URLConnection.getFileNameMap();
        String mimeType = fileNameMap.getContentTypeFor(file.getName());
        dto.setType(mimeType);

        byte[] attachmentBytes = new byte[0];
        try {
            attachmentBytes = Files.readAllBytes(Paths.get(attachement.getFileURI()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        String base64StringAttachment = Base64.getEncoder().encodeToString(attachmentBytes);
        dto.setContent(base64StringAttachment);

        return dto;
    }

}
