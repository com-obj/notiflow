package com.obj.nc.functions.processors.senders;

import com.obj.nc.aspects.DocumentProcessingInfo;
import com.obj.nc.domain.Attachement;
import com.obj.nc.domain.endpoints.EmailEndpoint;
import com.obj.nc.domain.message.Message;
import com.obj.nc.domain.message.MessageContent;
import com.obj.nc.domain.message.MessageContents;
import com.obj.nc.exceptions.ProcessingException;
import lombok.Data;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.File;
import java.util.Optional;
import java.util.function.Function;

@Component
@Log4j2
public class EmailSenderSinkExecution implements Function<Message, Message> {

	@Autowired
	private JavaMailSender emailSender;

	@Autowired
	private SendEmailMessageConfig config;

	@DocumentProcessingInfo("SendEmail")
	@Override
	public Message apply(Message payload) {
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
			MimeMessage message = emailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(message, true);

			helper.setFrom(config.getFrom());

			helper.setTo(toEmail.getEmail());

			helper.setSubject(messageContent.getSubject());
			helper.setText(messageContent.getText());

			for (Attachement attachement: messageContent.getAttachments()) {
				FileSystemResource file = new FileSystemResource(new File(attachement.getFileURI()));
				helper.addAttachment(attachement.getName(), file);
			}

			emailSender.send(message);
		} catch (MessagingException e) {
			throw new ProcessingException(EmailSenderSinkProcessingFunction.class, e);
		}
	}

	@ConfigurationProperties(prefix = "nc.functions.send-email-message")
	@Data
	@Component
	public static class SendEmailMessageConfig {

		String from;

	}

}