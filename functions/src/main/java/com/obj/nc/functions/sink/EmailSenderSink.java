package com.obj.nc.functions.sink;

import java.io.File;
import java.util.List;
import java.util.function.Consumer;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import com.obj.nc.domain.Attachement;
import com.obj.nc.domain.endpoints.EmailEndpoint;
import com.obj.nc.domain.endpoints.RecievingEndpoint;
import com.obj.nc.domain.message.Message;
import com.obj.nc.domain.message.MessageContent;
import com.obj.nc.exceptions.PayloadProcessingException;
import com.obj.nc.exceptions.PayloadValidationException;

import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Flux;

@Configuration
@Log4j2
public class EmailSenderSink {
	
	@Autowired
    private JavaMailSender emailSender;

	@Bean
	public Consumer<Flux<Message>> sendMessage() {
		return payloads -> payloads.doOnNext(payload -> sendMessage(payload)).subscribe();
	}

	public void sendMessage(Message payload) {
		List<RecievingEndpoint> to = payload.getBody().getRecievingEndpoints();
		if (to.size()!=1) {
			throw new PayloadValidationException("Email sender can send to only one recipient. Found more: " + to);
		}
		RecievingEndpoint endpoint = to.get(0);
		if (!(endpoint instanceof EmailEndpoint)) {
			throw new PayloadValidationException("Email sender can send to Email endpoints only. Found " + endpoint);
		}
		EmailEndpoint toEmail = (EmailEndpoint)endpoint;
		
		MessageContent msg = payload.getBody().getMessage();

		try {
			MimeMessage message = emailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(message, true);
	
			helper.setFrom("no-reply@objectify.sk");
	
			helper.setTo(toEmail.getEmail());
			
			helper.setSubject(msg.getSubject());
			helper.setText(msg.getText());
	
			for (Attachement attachement: payload.getBody().getAttachments()) {
				FileSystemResource file = new FileSystemResource(new File(attachement.getFileURI()));
				helper.addAttachment(attachement.getName(), file);
			}
	
			emailSender.send(message);
		} catch (MessagingException e) {
			throw new PayloadProcessingException(EmailSenderSink.class, e);
		}
	}

}
