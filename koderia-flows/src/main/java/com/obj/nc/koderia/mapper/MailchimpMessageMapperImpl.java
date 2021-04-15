package com.obj.nc.koderia.mapper;

import com.obj.nc.domain.Attachement;
import com.obj.nc.domain.content.Content;
import com.obj.nc.domain.content.email.EmailContent;
import com.obj.nc.domain.endpoints.EmailEndpoint;
import com.obj.nc.domain.endpoints.RecievingEndpoint;
import com.obj.nc.domain.message.Message;
import com.obj.nc.koderia.dto.koderia.event.BaseKoderiaEventDto;
import com.obj.nc.koderia.functions.processors.mailchimpSender.MailchimpSenderConfig;
import com.obj.nc.koderia.dto.mailchimp.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Component;

import static com.obj.nc.functions.processors.eventFactory.GenericEventToNotificaitonIntentConverter.ORIGINAL_EVENT_FIELD;
import static com.obj.nc.koderia.mapper.MailchimpMessageMapperImpl.COMPONENT_NAME;

import java.io.File;
import java.io.IOException;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;

@Component(COMPONENT_NAME)
public class MailchimpMessageMapperImpl implements MailchimpMessageMapper {

    public static final String COMPONENT_NAME = "mailchimpMessageMapper";
    public static final String SIMPLE_MESSAGE_FIELD = "SIMPLE_MESSAGE";

    @Autowired
    protected MailchimpSenderConfig mailchimpSenderConfig;

    @Override
    public SendMessageWithTemplateDto mapWithTemplate(Message message) {
        SendMessageWithTemplateDto dto = new SendMessageWithTemplateDto();
        dto.setKey(mailchimpSenderConfig.getMailchimpApi().getAuthKey());
        dto.setMessage(mapMessage(message));

        String messageTypeName = getTemplateName(message);
        dto.setTemplateName(messageTypeName);
        return dto;
    }

    protected MessageDto mapMessage(Message message) {
        MessageDto messageDto = new MessageDto();

        messageDto.setSubject(mapSubject(message));
        messageDto.setFromEmail(mailchimpSenderConfig.getMailchimpApi().getSenderEmail());
        messageDto.setFromName(mailchimpSenderConfig.getMailchimpApi().getSenderName());

        List<RecipientDto> recipients = Collections.singletonList(this.mapRecipient(message.getBody().getRecievingEndpoints().get(0)));
        messageDto.setTo(recipients);

        List<MergeVarDto> globalMergeVars = mapGlobalMergeVars(message);
        messageDto.setGlobalMergeVars(globalMergeVars);

        List<AttachmentDto> attachments = mapAttachments(message);
        messageDto.setAttachments(attachments);

        return messageDto;
    }

    protected String mapSubject(Message message) {
    	EmailContent messageContent = message.getContentTyped();
        return messageContent.getSubject();
    }

    protected List<MergeVarDto> mapGlobalMergeVars(Message message) {
        EmailContent messageContent = message.getContentTyped();
        BaseKoderiaEventDto originalEvent = messageContent.getAttributeValueAs(ORIGINAL_EVENT_FIELD, BaseKoderiaEventDto.class);

        Map<String, Object> mergeVars = new HashMap<>();
        mergeVars.put(SIMPLE_MESSAGE_FIELD, originalEvent.asMap());
        return mergeVars.entrySet().stream().map(this::mapMergeVar).collect(Collectors.toList());
    }

    protected <T> MergeVarDto mapMergeVar(Map.Entry<String, T> entry) {
        MergeVarDto dto = new MergeVarDto();
        dto.setName(entry.getKey());
        dto.setContent(entry.getValue());
        return dto;
    }

    protected List<AttachmentDto> mapAttachments(Message message) {
        EmailContent messageContent = message.getContentTyped();
        return messageContent.getAttachments().stream()
                .map(this::mapAttachment)
                .collect(Collectors.toList());
    }

    protected AttachmentDto mapAttachment(Attachement attachement) {
        AttachmentDto dto = new AttachmentDto();
        dto.setName(attachement.getName());

        FileSystemResource file = new FileSystemResource(new File(attachement.getFileURI()));
        FileNameMap fileNameMap = URLConnection.getFileNameMap();
        String mimeType = fileNameMap.getContentTypeFor(file.getFilename());
        dto.setType(mimeType);

        byte[] attachmentBytes = new byte[0];
        try {
            attachmentBytes = Files.readAllBytes(file.getFile().toPath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        String base64StringAttachment = Base64.getEncoder().encodeToString(attachmentBytes);
        dto.setContent(base64StringAttachment);

        return dto;
    }

    protected String getTemplateName(Message message) {
        Content messageContent = message.getBody().getMessage();
        BaseKoderiaEventDto originalEvent = messageContent.getAttributeValueAs(ORIGINAL_EVENT_FIELD, BaseKoderiaEventDto.class);
        return mailchimpSenderConfig.getTemplateNameFromMessageType(originalEvent.getTypeName());
    }

    protected RecipientDto mapRecipient(RecievingEndpoint endpoint) {
        if (!EmailEndpoint.JSON_TYPE_IDENTIFIER.equals(endpoint.getEndpointType())) {
            throw new UnsupportedOperationException("Mapper can only map EmailContent endpoint");
        }

        RecipientDto dto = new RecipientDto();
        dto.setName(endpoint.getRecipient().getName());
        dto.setEmail(((EmailEndpoint) endpoint).getEmail());
        return dto;
    }

}
