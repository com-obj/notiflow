package com.obj.nc.functions.processors.senders;

import java.io.File;
import java.util.List;
import java.util.Optional;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.springframework.context.annotation.Primary;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import com.obj.nc.aspects.DocumentProcessingInfo;
import com.obj.nc.domain.Attachement;
import com.obj.nc.domain.endpoints.EmailEndpoint;
import com.obj.nc.domain.endpoints.RecievingEndpoint;
import com.obj.nc.domain.message.Message;
import com.obj.nc.domain.message.MessageContent;
import com.obj.nc.domain.message.MessageContents;
import com.obj.nc.exceptions.PayloadValidationException;
import com.obj.nc.exceptions.ProcessingException;
import com.obj.nc.functions.processors.ProcessorFunctionAdapter;

import lombok.AllArgsConstructor;

@Primary
@Component
@AllArgsConstructor
public class EmailSender extends ProcessorFunctionAdapter<Message, Message> {
	

	private final JavaMailSenderImpl javaMailSender;
	
	@Override
	public Optional<PayloadValidationException> checkPreCondition(Message message) {
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
		EmailEndpoint toEmail = (EmailEndpoint) payload.getBody().getRecievingEndpoints().get(0);

		MessageContents msg = payload.getBody().getMessage();

		Optional<MessageContent> messageContentOpt = Optional.empty();

		if (payload.isAggregateMessage()) {
			messageContentOpt = msg.getAggregateContent().stream()
					.reduce(MessageContent::concat);
		} else {
			messageContentOpt = Optional.of(msg.getContent());
		}

		MessageContent messageContent = messageContentOpt
				.orElseThrow(() -> new IllegalStateException("Failed to build message content"));

		doSendMessage(toEmail, messageContent);
		return payload;
	}

	private void doSendMessage(EmailEndpoint toEmail, MessageContent messageContent) {
		try {
			MimeMessage message = javaMailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(message, true);

			helper.setFrom(javaMailSender.getUsername());

			helper.setTo(toEmail.getEmail());

			helper.setSubject(messageContent.getSubject());
			helper.setText(messageContent.getText());

			for (Attachement attachement: messageContent.getAttachments()) {
				FileSystemResource file = new FileSystemResource(new File(attachement.getFileURI()));
				helper.addAttachment(attachement.getName(), file);
			}

			javaMailSender.send(message);
		} catch (MessagingException e) {
			throw new ProcessingException(EmailSender.class, e);
		}
	}


}
