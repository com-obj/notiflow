package com.obj.nc.functions.processors.senders;

import java.io.File;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.springframework.context.annotation.Primary;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import com.obj.nc.aspects.DocumentProcessingInfo;
import com.obj.nc.domain.Attachement;
import com.obj.nc.domain.Header;
import com.obj.nc.domain.content.AggregatedEmail;
import com.obj.nc.domain.content.EmailContent;
import com.obj.nc.domain.endpoints.EmailEndpoint;
import com.obj.nc.domain.endpoints.RecievingEndpoint;
import com.obj.nc.domain.message.Message;
import com.obj.nc.exceptions.PayloadValidationException;
import com.obj.nc.exceptions.ProcessingException;
import com.obj.nc.functions.processors.ProcessorFunctionAdapter;
import com.obj.nc.utils.JsonUtils;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Primary
@Component
@AllArgsConstructor
@Log4j2
public class EmailSender extends ProcessorFunctionAdapter<Message, Message> {
	
	private final JavaMailSenderImpl mailSender;
	
	public static String NOTIF_CENTER_EMAIL_HEANDER_PREFIX = "$NC_";
	
	@Override
	public Optional<PayloadValidationException> checkPreCondition(Message message) {
		if (!(message.getBody().getMessage() instanceof EmailContent)) {
			return Optional.of(new PayloadValidationException("EmailContent sender can process only Message with EmailContent content. Was type " + message.getBody().getMessage().getClass().getSimpleName()));
		}
		
		List<RecievingEndpoint> to = message.getBody().getRecievingEndpoints();

		if (to.size() != 1) {
			return Optional.of(new PayloadValidationException("EmailContent sender can send to only one recipient. Found more: " + to));
		}

		RecievingEndpoint endpoint = to.get(0);
		if (!(endpoint instanceof EmailEndpoint)) {
			return Optional.of(new PayloadValidationException("EmailContent sender can send to EmailContent endpoints only. Found " + endpoint));
		}

		return Optional.empty();
	}


	@DocumentProcessingInfo("SendEmail")
	@Override
	public Message execute(Message payload) {
		payload.stepStart("SendEmail");
		
		EmailEndpoint toEmail = (EmailEndpoint) payload.getBody().getRecievingEndpoints().get(0);

		EmailContent msg = payload.getContentTyped();

		EmailContent messageContent = null;
		if (msg instanceof AggregatedEmail) {
			//ak je stale v rezime aggregated tak mi nic ine nezostava ako spravit "dummy" aggregation. Na konci dna potrebujem jeden subject, jeden text
			messageContent = ((AggregatedEmail) msg).asSimpleContent();
		} else {
			messageContent = msg;
		}

		doSendMessage(toEmail, messageContent, payload.getHeader());
		
		payload.stepFinish();
		return payload;
	}

	private void doSendMessage(EmailEndpoint toEmail, EmailContent messageContent, Header header) {
		try {
			MimeMessage message = mailSender.createMimeMessage();	
			copyHeaderValues(header, message);
			
			MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

			helper.setFrom(mailSender.getUsername());

			helper.setTo(toEmail.getEmail());

			helper.setSubject(messageContent.getSubject());
			helper.setText(messageContent.getText(), MediaType.TEXT_HTML_VALUE.equals(messageContent.getContentType()) );
			
			for (Attachement attachement: messageContent.getAttachments()) {
				FileSystemResource file = new FileSystemResource(new File(attachement.getFileURI()));
				helper.addAttachment(attachement.getName(), file);
			}

			Instant sendStart = Instant.now();
			
			mailSender.send(message);
			
			log.info("Sending mail vie SMTP took {} ms", ChronoUnit.MILLIS.between(sendStart, Instant.now()));
			
		} catch (MessagingException e) {
			throw new ProcessingException(EmailSender.class, e);
		}
	}


	private void copyHeaderValues(Header header, MimeMessage message) {
		header.getAttributes().entrySet().forEach(entry-> {
			try {
				message.setHeader(NOTIF_CENTER_EMAIL_HEANDER_PREFIX + entry.getKey(), entry.getValue()+"");
			} catch (MessagingException e) {
				throw new RuntimeException(e);
			}
		});
		
		try {
			message.setHeader(NOTIF_CENTER_EMAIL_HEANDER_PREFIX + "EVENT_ID", JsonUtils.writeObjectToJSONString(header.getEventIds()));
			
			if (header.getFlowId()!= null) {
				message.setHeader(NOTIF_CENTER_EMAIL_HEANDER_PREFIX + "FLOW_ID", header.getFlowId());
			}
			if (header.getId()!= null) {
				message.setHeader(NOTIF_CENTER_EMAIL_HEANDER_PREFIX + "MSG_ID", header.getId().toString());
			}
		} catch (MessagingException e) {
			throw new RuntimeException(e);
		}
	}


}
