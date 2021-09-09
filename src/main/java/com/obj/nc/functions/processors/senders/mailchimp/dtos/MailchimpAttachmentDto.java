package com.obj.nc.functions.processors.senders.mailchimp.dtos;

import org.apache.commons.codec.binary.Base64InputStream;
import org.springframework.core.io.FileSystemResource;

import com.obj.nc.domain.Attachment;
import com.obj.nc.utils.FileUtils;

import lombok.Data;

@Data
public class MailchimpAttachmentDto {
	
	public static final long MAX_ATTACHMENT_SIZE = 25 * (1 << 20);

	private String type;
	private String name;
	private String content;

	public static MailchimpAttachmentDto fromAttachment(Attachment attachment) {
		MailchimpAttachmentDto dto = new MailchimpAttachmentDto();
		dto.setName(attachment.getName());

		FileSystemResource file;
		if (attachment.getFilePathAndName()!=null) {
			file = FileUtils.newFileSystemResource(attachment.getFilePathAndName());
		} else {
			file = FileUtils.newFileSystemResource(attachment.getFileURI());		
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
