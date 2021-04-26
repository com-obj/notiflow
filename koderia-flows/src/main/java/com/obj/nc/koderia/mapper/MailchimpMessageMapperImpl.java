package com.obj.nc.koderia.mapper;

import static com.obj.nc.koderia.functions.processors.KoderiaEventConverterExecution.ORIGINAL_EVENT_FIELD;
import static com.obj.nc.koderia.mapper.MailchimpMessageMapperImpl.COMPONENT_NAME;

import java.io.File;
import java.io.IOException;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.nio.file.Files;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Component;

import com.obj.nc.domain.Attachement;
import com.obj.nc.domain.content.Content;
import com.obj.nc.domain.content.email.EmailContent;
import com.obj.nc.domain.endpoints.EmailEndpoint;
import com.obj.nc.domain.endpoints.RecievingEndpoint;
import com.obj.nc.domain.message.Message;
import com.obj.nc.koderia.config.MailchimpApiConfig;
import com.obj.nc.koderia.dto.EmitEventDto;
import com.obj.nc.koderia.dto.mailchimp.AttachmentDto;
import com.obj.nc.koderia.dto.mailchimp.MergeVarDto;
import com.obj.nc.koderia.dto.mailchimp.MessageDto;
import com.obj.nc.koderia.dto.mailchimp.RecipientDto;
import com.obj.nc.koderia.dto.mailchimp.SendMessageWithTemplateDto;

@Component(COMPONENT_NAME)
public class MailchimpMessageMapperImpl implements MailchimpMessageMapper {

    public static final String COMPONENT_NAME = "mailchimpMessageMapper";
    public static final String EVENT_FIELD = "event";

    @Autowired
    protected MailchimpApiConfig mailchimpApiConfig;

    @Override
    public SendMessageWithTemplateDto mapWithTemplate(Message message) {
        SendMessageWithTemplateDto dto = new SendMessageWithTemplateDto();
        dto.setKey(mailchimpApiConfig.getApi().getKey());
        dto.setMessage(mapMessage(message));

        String messageTypeName = getTemplateName(message);
        dto.setTemplateName(messageTypeName);
        return dto;
    }

    protected MessageDto mapMessage(Message message) {
        MessageDto messageDto = new MessageDto();

        messageDto.setSubject(mapSubject(message));
        messageDto.setFromEmail(mailchimpApiConfig.getApi().getFromEmail());
        messageDto.setFromName(mailchimpApiConfig.getApi().getFromName());

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
        Content messageContent = message.getBody().getMessage();
        EmitEventDto originalEvent = messageContent.getAttributeValueAs(ORIGINAL_EVENT_FIELD, EmitEventDto.class);

        Map<String, Object> mergeVars = new HashMap<>();
        mergeVars.put(EVENT_FIELD, originalEvent.asMap());
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
        EmitEventDto originalEvent = messageContent.getAttributeValueAs(ORIGINAL_EVENT_FIELD, EmitEventDto.class);
        return mailchimpApiConfig.getTemplateNameFromMessageType(originalEvent.getType());
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
