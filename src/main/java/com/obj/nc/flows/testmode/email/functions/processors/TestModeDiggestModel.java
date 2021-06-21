package com.obj.nc.flows.testmode.email.functions.processors;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;

import com.obj.nc.domain.content.email.EmailContent;
import com.obj.nc.domain.content.sms.SimpleTextContent;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

@Data
@EqualsAndHashCode
@RequiredArgsConstructor
public class TestModeDiggestModel {
	
	private List<EmailContent> emailContents = new ArrayList<>();
	private List<SimpleTextContent> smsContents = new ArrayList<>();

	public void addEmailContent(EmailContent emailContent) {
		emailContents.add(emailContent);
		if (!emailContent.getContentType().equals(MediaType.TEXT_HTML_VALUE)) {
			return;
		}
		
		String bodyPartHtml = StringUtils.substringBetween(emailContent.getText(), "<body>", "</body>");
		emailContent.setText("<div>" + bodyPartHtml + "</div>");
	}

	public void addSmsContent(SimpleTextContent smsContent) {
		smsContents.add(smsContent);
	}
	
}
