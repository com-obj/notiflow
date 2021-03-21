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
import com.obj.nc.domain.endpoints.EmailEndpoint;
import com.obj.nc.domain.endpoints.RecievingEndpoint;
import com.obj.nc.domain.message.Message;
import com.obj.nc.domain.message.Content;
import com.obj.nc.domain.message.Email;
import com.obj.nc.domain.message.AggregatedEmail;
import com.obj.nc.exceptions.PayloadValidationException;
import com.obj.nc.exceptions.ProcessingException;
import com.obj.nc.functions.processors.ProcessorFunctionAdapter;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Primary
@Component
@AllArgsConstructor
@Log4j2
public class EmailSender extends ProcessorFunctionAdapter<Message, Message> {
	
	private final JavaMailSenderImpl mailSender;
	
	@Override
	public Optional<PayloadValidationException> checkPreCondition(Message message) {
		if (!(message.getBody().getMessage() instanceof Email)) {
			return Optional.of(new PayloadValidationException("Email sender can process only Message with Email content. Was type " + message.getBody().getMessage().getClass().getSimpleName()));
		}
		
		List<RecievingEndpoint> to = message.getBody().getRecievingEndpoints();

		if (to.size() != 1) {
			return Optional.of(new PayloadValidationException("Email sender can send to only one recipient. Found more: " + to));
		}

		RecievingEndpoint endpoint = to.get(0);
		if (!(endpoint instanceof EmailEndpoint)) {
			return Optional.of(new PayloadValidationException("Email sender can send to Email endpoints only. Found " + endpoint));
		}

		return Optional.empty();
	}


	@DocumentProcessingInfo("SendEmail")
	@Override
	public Message execute(Message payload) {
		payload.stepStart("SendEmail");
		
		EmailEndpoint toEmail = (EmailEndpoint) payload.getBody().getRecievingEndpoints().get(0);

		Email msg = payload.getContentTyped();

		Email messageContent = null;
		if (msg instanceof AggregatedEmail) {
			//ak je stale v rezime aggregated tak mi nic ine nezostava ako spravit "dummy" aggregation. Na konci dna potrebujem jeden subject, jeden text
			messageContent = ((AggregatedEmail) msg).asSimpleContent();
		} else {
			messageContent = msg;
		}

		doSendMessage(toEmail, messageContent);
		
		payload.stepFinish();
		return payload;
	}

	private void doSendMessage(EmailEndpoint toEmail, Email messageContent) {
		try {
			MimeMessage message = mailSender.createMimeMessage();
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


}
