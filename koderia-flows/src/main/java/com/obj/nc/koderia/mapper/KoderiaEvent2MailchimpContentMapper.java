package com.obj.nc.koderia.mapper;

import com.obj.nc.domain.Attachement;
import com.obj.nc.domain.content.mailchimp.*;
import com.obj.nc.koderia.domain.event.BaseKoderiaEvent;
import com.obj.nc.functions.processors.senders.mailchimp.MailchimpSenderConfig;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;

import static com.obj.nc.domain.content.mailchimp.MailchimpContent.DATA_MERGE_VARIABLE;

@Component
@RequiredArgsConstructor
public class KoderiaEvent2MailchimpContentMapper {
    private final MailchimpSenderConfig mailchimpSenderConfig;

    public MailchimpContent map(BaseKoderiaEvent event) {
        MailchimpContent dto = new MailchimpContent();
        dto.setSubject(event.getMessageSubject());
        dto.setText(event.getMessageText());
        dto.setAttachments(event.getAttachments());
        
        dto.setKey(mailchimpSenderConfig.getAuthKey());
        dto.setMessage(mapMessage(event));

        String messageTypeName = getTemplateName(event);
        dto.setTemplateName(messageTypeName);
        return dto;
    }

    protected MailchimpMessage mapMessage(BaseKoderiaEvent event) {
        MailchimpMessage mailchimpMessageDto = new MailchimpMessage();

        mailchimpMessageDto.setSubject(mapSubject(event));
        mailchimpMessageDto.setFromEmail(mailchimpSenderConfig.getSenderEmail());
        mailchimpMessageDto.setFromName(mailchimpSenderConfig.getSenderName());

        mailchimpMessageDto.setTo(new ArrayList<>());

        List<MailchimpMergeVariable> globalMergeVars = mapGlobalMergeVars(event);
        mailchimpMessageDto.setGlobalMergeVars(globalMergeVars);

        List<MailchimpAttachment> mailchimpAttachments = mapAttachments(event);
        mailchimpMessageDto.setAttachments(mailchimpAttachments);

        return mailchimpMessageDto;
    }

    protected String mapSubject(BaseKoderiaEvent event) {
        return event.getMessageSubject();
    }

    protected List<MailchimpMergeVariable> mapGlobalMergeVars(BaseKoderiaEvent message) {
        Map<String, Object> mergeVars = new HashMap<>();
        mergeVars.put(DATA_MERGE_VARIABLE, message);
        return mergeVars.entrySet().stream().map(this::mapMergeVar).collect(Collectors.toList());
    }

    protected <T> MailchimpMergeVariable mapMergeVar(Map.Entry<String, T> entry) {
        MailchimpMergeVariable mergeVar = new MailchimpMergeVariable();
        mergeVar.setName(entry.getKey());
        mergeVar.setContent(entry.getValue());
        return mergeVar;
    }

    protected List<MailchimpAttachment> mapAttachments(BaseKoderiaEvent event) {
        return event.getAttachments().stream()
                .map(this::mapAttachment)
                .collect(Collectors.toList());
    }

    protected MailchimpAttachment mapAttachment(Attachement attachement) {
        MailchimpAttachment dto = new MailchimpAttachment();
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

    protected String getTemplateName(BaseKoderiaEvent event) {
        return mailchimpSenderConfig.getTemplateNameFromMessageType(event.getTypeName());
    }

}
