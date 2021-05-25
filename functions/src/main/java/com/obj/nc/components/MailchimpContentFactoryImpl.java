package com.obj.nc.components;

import java.io.File;
import java.io.IOException;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.nio.file.Files;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Component;

import com.obj.nc.components.api.MailchimpContentFactory;
import com.obj.nc.domain.Attachement;
import com.obj.nc.domain.content.mailchimp.MailchimpAttachment;
import com.obj.nc.domain.content.mailchimp.MailchimpContent;
import com.obj.nc.domain.content.mailchimp.MailchimpData;
import com.obj.nc.functions.processors.senders.mailchimp.MailchimpMergeVarMapper;
import com.obj.nc.functions.processors.senders.mailchimp.MailchimpSenderConfigProperties;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class MailchimpContentFactoryImpl implements MailchimpContentFactory {
    
    private final MailchimpSenderConfigProperties mailchimpSenderConfigProperties;
    private final MailchimpMergeVarMapper mailchimpMergeVarMapper;

    public MailchimpContent createFromData(MailchimpData event) {
        MailchimpContent content = new MailchimpContent();
        content.setOriginalEvent(event);
        content.setSubject(mapSubject(event));
        content.setSenderName(mailchimpSenderConfigProperties.getSenderName());
        content.setSenderEmail(mailchimpSenderConfigProperties.getSenderEmail());
        content.setTemplateName(mailchimpSenderConfigProperties.getTemplateNameFromMessageType(event.getType()));
        content.setGlobalMergeVariables(mailchimpMergeVarMapper.map(event));
        content.setMergeLanguage(mailchimpSenderConfigProperties.getMergeLanguage());
        
        List<MailchimpAttachment> mailchimpAttachments = mapAttachments(event);
        content.setAttachments(mailchimpAttachments);
        return content;
    }

    protected String mapSubject(MailchimpData event) {
        return event.getSubject();
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

}
