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

package com.obj.nc.functions.processors.senders;

import java.io.File;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import com.obj.nc.domain.Attachment;
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import com.obj.nc.aspects.DocumentProcessingInfo;
import com.obj.nc.domain.content.email.EmailContent;
import com.obj.nc.domain.endpoints.EmailEndpoint;
import com.obj.nc.domain.endpoints.ReceivingEndpoint;
import com.obj.nc.domain.message.EmailMessage;
import com.obj.nc.exceptions.PayloadValidationException;
import com.obj.nc.exceptions.ProcessingException;
import com.obj.nc.functions.processors.ProcessorFunctionAdapter;
import com.obj.nc.functions.processors.senders.config.EmailSenderConfigProperties;
import com.obj.nc.utils.JsonUtils;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Primary
@Component
@AllArgsConstructor
@Log4j2
@DocumentProcessingInfo("SendEmail")
public class EmailSender extends ProcessorFunctionAdapter<EmailMessage, EmailMessage> {
	
	private final JavaMailSenderImpl mailSender;
	
	public static String NOTIF_CENTER_EMAIL_HEANDER_PREFIX = "$NC_";
	public static String EVENT_IDS_EMAIL_HEANDER = NOTIF_CENTER_EMAIL_HEANDER_PREFIX+ "EVENT_IDS";
	public static String FLOW_ID_EMAIL_HEANDER = NOTIF_CENTER_EMAIL_HEANDER_PREFIX+ "FLOW_ID";
	
	private final EmailSenderConfigProperties settings;
	
	@Override
	public Optional<PayloadValidationException> checkPreCondition(EmailMessage message) {
		if (!(message.getBody() instanceof EmailContent)) {
			return Optional.of(new PayloadValidationException("EmailContent sender can process only Message with EmailContent content. Message was: " + message));
		}
		
		List<? extends ReceivingEndpoint> to = message.getReceivingEndpoints();

		if (to.size() != 1) {
			return Optional.of(new PayloadValidationException("EmailContent sender can send to only one recipient. Found more: " + to));
		}

		ReceivingEndpoint endpoint = to.get(0);
		if (!(endpoint instanceof EmailEndpoint)) {
			return Optional.of(new PayloadValidationException("EmailContent sender can send to EmailEndpoint endpoints only. Found " + endpoint));
		}

		return Optional.empty();
	}



	@Override
	public EmailMessage execute(EmailMessage payload) {		
		doSendMessage(payload);
		return payload;
	}

	private void doSendMessage(EmailMessage payload) {
		try {
			MimeMessage message = mailSender.createMimeMessage();
			copyHeaderValuesToMimeMessage(payload, message);
			
			MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

			if (settings.getFromMailAddress()!=null) {
				helper.setFrom(settings.getFromMailAddress());
			}

			helper.setTo(payload.getReceivingEndpoints().get(0).getEmail());

			helper.setSubject(payload.getBody().getSubject());
			boolean isHtml = MediaType.TEXT_HTML_VALUE.equals(payload.getBody().getContentType());
			
			if (isHtml) {
				helper.setText(StringEscapeUtils.unescapeHtml4( payload.getBody().getText() ), true );
			} else {
				helper.setText(payload.getBody().getText() );
			}
			
			for (Attachment attachment : payload.getBody().getAttachments()) {
				FileSystemResource file = new FileSystemResource(new File(attachment.getFileURI()));
				helper.addAttachment(attachment.getName(), file);
			}

			Instant sendStart = Instant.now();
			
			mailSender.send(message);
			
			log.info("Sending mail vie SMTP took {} ms", ChronoUnit.MILLIS.between(sendStart, Instant.now()));
			
		} catch (MessagingException e) {
			throw new ProcessingException(EmailSender.class, e);
		}
	}

	private void copyHeaderValuesToMimeMessage(EmailMessage payload, MimeMessage message) {
		payload.getAttributes().entrySet().forEach(entry-> {
			try {
				message.setHeader(NOTIF_CENTER_EMAIL_HEANDER_PREFIX + entry.getKey(), entry.getValue()+"");
			} catch (MessagingException e) {
				throw new RuntimeException(e);
			}
		});
		
		try {
			message.setHeader(EVENT_IDS_EMAIL_HEANDER, JsonUtils.writeObjectToJSONString(payload.getPreviousEventIds()));
			
			if (payload.getHeader().getFlowId()!= null) {
				message.setHeader(FLOW_ID_EMAIL_HEANDER, payload.getHeader().getFlowId());
			}
		} catch (MessagingException e) {
			throw new RuntimeException(e);
		}
	}


}
