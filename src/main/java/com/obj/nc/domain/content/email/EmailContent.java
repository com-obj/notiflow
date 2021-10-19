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

package com.obj.nc.domain.content.email;

import com.obj.nc.domain.Attachment;
import com.obj.nc.domain.content.MessageContent;
import com.obj.nc.domain.content.TrackableContent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.http.MediaType;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class EmailContent extends MessageContent implements TrackableContent {
	
	public static final String TEXT_CONCAT_DELIMITER = "\n\n";
	public static final String SUBJECT_CONCAT_DELIMITER = ", ";

	@EqualsAndHashCode.Include
	private String subject;
	
	@EqualsAndHashCode.Include
	private String text;
	
	@EqualsAndHashCode.Include
	@Builder.Default
	private String contentType=MediaType.TEXT_PLAIN_VALUE;

	@EqualsAndHashCode.Include
	@Builder.Default
	private List<Attachment> attachments = new ArrayList();
	
	@Override
	public boolean hasHtmlText() {
		return MediaType.TEXT_HTML_VALUE.equals(contentType) && getText() != null;
	}
	
	@Override
	public String getHtmlText() {
		return getText();
	}
	
	@Override
	public void setHtmlText(String text) {
		setText(text);
	}
	
}
