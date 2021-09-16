/*
 *   Copyright (C) 2021 the original author or authors.
 *
 *   This file is part of Notiflow
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Lesser General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public License
 *   along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

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
