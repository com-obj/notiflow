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
import static com.obj.nc.mapper.MailchimpMessageMapperImpl.COMPONENT_NAME;

@Component(COMPONENT_NAME)
public class MailchimpMessageMapperImpl implements MailchimpMessageMapper {

    public static final String COMPONENT_NAME = "mailchimpMessageMapper";

    @Autowired
    protected MailchimpApiConfig mailchimpApiConfig;

    @Override
    public SendMessageWithTemplateDto mapWithTemplate(Message message) {
        SendMessageWithTemplateDto dto = new SendMessageWithTemplateDto();
        dto.setKey(mailchimpApiConfig.getApi().getKey());
        dto.setMessage(mapMessage(message));

        String messageTypeName = getTemplateName(message);
        dto.setTemplate_name(messageTypeName);
        return dto;
    }

    protected MessageDto mapMessage(Message message) {
        MessageDto messageDto = new MessageDto();

        messageDto.setSubject(mapSubject(message));
        messageDto.setFrom_email(mailchimpApiConfig.getApi().getFromEmail());
        messageDto.setFrom_name(mailchimpApiConfig.getApi().getFromName());

        List<RecipientDto> recipients = Collections.singletonList(this.mapRecipient(message.getBody().getRecievingEndpoints().get(0)));
        messageDto.setTo(recipients);

        List<MergeVarDto> globalMergeVars = mapGlobalMergeVars(message);
        messageDto.setGlobal_merge_vars(globalMergeVars);

        List<AttachmentDto> attachments = mapAttachments(message);
        messageDto.setAttachments(attachments);

        return messageDto;
    }

    protected String mapSubject(Message message) {
        MessageContent messageContent = message.getBody().getMessage().getContent();
        return messageContent.getSubject();
    }

    protected List<MergeVarDto> mapGlobalMergeVars(Message message) {
        MessageContent messageContent = message.getBody().getMessage().getContent();
        EmitEventDto originalEvent = messageContent.getAttributeValueAs(ORIGINAL_EVENT_FIELD, EmitEventDto.class);
        return originalEvent.asMap().entrySet().stream()
                .map(this::mapMergeVar)
                .collect(Collectors.toList());
    }

    protected <T> MergeVarDto mapMergeVar(Map.Entry<String, T> entry) {
        MergeVarDto dto = new MergeVarDto();
        dto.setName(entry.getKey());
        dto.setContent(entry.getValue());
        return dto;
    }

    protected List<AttachmentDto> mapAttachments(Message message) {
        MessageContent messageContent = message.getBody().getMessage().getContent();
        return messageContent.getAttachments().stream()
                .map(this::mapAttachment)
                .collect(Collectors.toList());
    }

    protected AttachmentDto mapAttachment(Attachement attachement) {
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

    protected String getTemplateName(Message message) {
        MessageContent messageContent = message.getBody().getMessage().getContent();
        EmitEventDto originalEvent = messageContent.getAttributeValueAs(ORIGINAL_EVENT_FIELD, EmitEventDto.class);
        return mailchimpApiConfig.getTemplateNameFromMessageType(originalEvent.getType());
    }

    protected RecipientDto mapRecipient(RecievingEndpoint endpoint) {
        if (!EmailEndpoint.JSON_TYPE_IDENTIFIER.equals(endpoint.getEndpointTypeName())) {
            throw new UnsupportedOperationException("Mapper can only map Email endpoint");
        }

        RecipientDto dto = new RecipientDto();
        dto.setName(endpoint.getRecipient().getName());
        dto.setEmail(((EmailEndpoint) endpoint).getEmail());
        return dto;
    }

}
