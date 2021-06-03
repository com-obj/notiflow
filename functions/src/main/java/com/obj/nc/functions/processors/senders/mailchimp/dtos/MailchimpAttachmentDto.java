package com.obj.nc.functions.processors.senders.mailchimp.dtos;

import java.io.File;
import java.io.IOException;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.nio.file.Files;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.core.io.FileSystemResource;

import com.obj.nc.domain.Attachement;

import lombok.Data;

@Data
public class MailchimpAttachmentDto {

	private String type;
	private String name;
	private String content;
	
	public static List<MailchimpAttachmentDto> fromAttachements(List<Attachement> attachements) {
        return attachements.stream()
                .map(MailchimpAttachmentDto::fromAttachement)
                .collect(Collectors.toList());
    }

	public static MailchimpAttachmentDto fromAttachement(Attachement attachement) {
		MailchimpAttachmentDto dto = new MailchimpAttachmentDto();
		dto.setName(attachement.getName());

		FileSystemResource file = new FileSystemResource(new File(attachement.getFileURI()));
		FileNameMap fileNameMap = URLConnection.getFileNameMap();
		String mimeType = fileNameMap.getContentTypeFor(file.getFilename());
		dto.setType(mimeType);

		//TODO: implement in stream fashion. This could be very demanding on memory
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

}
