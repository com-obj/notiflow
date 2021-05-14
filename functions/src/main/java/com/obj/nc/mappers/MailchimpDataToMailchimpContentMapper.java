package com.obj.nc.mappers;

import com.obj.nc.domain.Attachement;
import com.obj.nc.domain.content.mailchimp.*;

import com.obj.nc.functions.processors.senders.mailchimp.MailchimpSenderConfigProperties;
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

@Component
@RequiredArgsConstructor
public class MailchimpDataToMailchimpContentMapper {
    private final MailchimpSenderConfigProperties mailchimpSenderConfigProperties;

    public MailchimpContent map(MailchimpData event) {
        MailchimpContent content = new MailchimpContent();
        content.setMessage(mapMessage(event));
        content.setTemplateName(getTemplateName(event));
        return content;
    }

    protected MailchimpMessage mapMessage(MailchimpData event) {
        MailchimpMessage mailchimpMessage = new MailchimpMessage();
        mailchimpMessage.setOriginalEvent(event);

        mailchimpMessage.setSubject(mapSubject(event));
        mailchimpMessage.setFromEmail(mailchimpSenderConfigProperties.getSenderEmail());
        mailchimpMessage.setFromName(mailchimpSenderConfigProperties.getSenderName());

        List<MailchimpMergeVariable> globalMergeVars = mapGlobalMergeVars(event);
        mailchimpMessage.setGlobalMergeVars(globalMergeVars);

        List<MailchimpAttachment> mailchimpAttachments = mapAttachments(event);
        mailchimpMessage.setAttachments(mailchimpAttachments);

        return mailchimpMessage;
    }

    protected String mapSubject(MailchimpData event) {
        return event.getMessageSubject();
    }

    protected List<MailchimpMergeVariable> mapGlobalMergeVars(MailchimpData message) {
        MailchimpMergeVariable mergeVar = new MailchimpMergeVariable();
        mergeVar.setName(message.getType());
        mergeVar.setContent(message);
        
        List<MailchimpMergeVariable> result = new ArrayList<>();
        result.add(mergeVar);
        return result;
    }

    protected List<MailchimpAttachment> mapAttachments(MailchimpData event) {
        return event.getAttachments().stream()
                .map(this::mapAttachment)
                .collect(Collectors.toList());
    }

    protected MailchimpAttachment mapAttachment(Attachement attachement) {
        MailchimpAttachment attachment = new MailchimpAttachment();
        attachment.setName(attachement.getName());

        FileSystemResource file = new FileSystemResource(new File(attachement.getFileURI()));
        FileNameMap fileNameMap = URLConnection.getFileNameMap();
        String mimeType = fileNameMap.getContentTypeFor(file.getFilename());
        attachment.setType(mimeType);

        byte[] attachmentBytes = new byte[0];
        try {
            attachmentBytes = Files.readAllBytes(file.getFile().toPath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        String base64StringAttachment = Base64.getEncoder().encodeToString(attachmentBytes);
        attachment.setContent(base64StringAttachment);

        return attachment;
    }

    protected String getTemplateName(MailchimpData event) {
        return mailchimpSenderConfigProperties.getTemplateNameFromMessageType(event.getType());
    }

}
