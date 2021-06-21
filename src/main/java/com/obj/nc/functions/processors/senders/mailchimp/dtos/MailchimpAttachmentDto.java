package com.obj.nc.functions.processors.senders.mailchimp.dtos;

import java.util.List;
import java.util.stream.Collectors;

import com.obj.nc.utils.FileUtils;
import org.apache.commons.codec.binary.Base64InputStream;
import org.springframework.core.io.FileSystemResource;

import com.obj.nc.domain.Attachement;

import lombok.Data;

@Data
public class MailchimpAttachmentDto {
	
	public static final long MAX_ATTACHMENT_SIZE = 25 * (1 << 20);

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

		FileSystemResource file;
		if (attachement.getFilePathAndName()!=null) {
			file = FileUtils.newFileSystemResource(attachement.getFilePathAndName());
		} else {
			file = FileUtils.newFileSystemResource(attachement.getFileURI());		
		}
		
		long fileSize = FileUtils.fileSize(file);
		if (fileSize > MAX_ATTACHMENT_SIZE) {
			throw new RuntimeException(String.format("Maximum file size exceeded. Maximum=%dMB, actual=%dMB", MAX_ATTACHMENT_SIZE/(1 << 20), fileSize/(1 << 20)));
		}
		
		dto.setType(FileUtils.mimeType(file).getType());
		
		Base64InputStream base64InputStream = FileUtils.base64InputStream(file);
		dto.setContent(FileUtils.inputStreamToString(base64InputStream));
		
		return dto;
	}
	
}
