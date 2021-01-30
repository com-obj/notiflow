package com.obj.nc.functions.processors.senders;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.UUID;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.ActiveProfiles;

import com.icegreen.greenmail.util.GreenMail;
import com.obj.nc.domain.ProcessingInfo;
import com.obj.nc.domain.message.Message;
import com.obj.nc.exceptions.PayloadValidationException;
import com.obj.nc.functions.processors.senders.EmailSenderSink.SendEmailMessage;
import com.obj.nc.functions.processors.senders.EmailSenderSink.SendEmailMessageConfig;
import com.obj.nc.utils.GreenMailManager;
import com.obj.nc.utils.JsonUtils;

@SpringBootTest(webEnvironment = WebEnvironment.NONE)
@ActiveProfiles(profiles = "test")
class EmailSenderSinkTest {
	
	@Autowired 
	private GreenMailManager greenMailManager;
	
	@Autowired 
	private SendEmailMessage functionSend;

	@Autowired
	private EmailSenderSink.CheckPreConditions checkPreConditions;
	
	@Autowired
	private SendEmailMessageConfig emailFromSetting;


	@Test
	void sendSingleMail() throws MessagingException, IOException {
		//GIVEN
		String INPUT_JSON_FILE = "messages/email_message.json";
		Message message = JsonUtils.readObjectFromClassPathResource(INPUT_JSON_FILE, Message.class);
		UUID originalProcessingId = message.getProcessingInfo().getProcessingId();
		
		//WHEN
		Message result = functionSend.apply(message);
		
		//THEN		 
		GreenMail gm = greenMailManager.getGreenMail();
		MimeMessage[] messages = gm.getReceivedMessages();
		assertThat( messages.length ).isEqualTo(1);
		assertThat( messages[0].getSubject() ).isEqualTo("Subject");
		assertThat( messages[0].getFrom()[0] ).extracting("address").isEqualTo(emailFromSetting.getFrom());
		
		//THEN check processing info
		
		ProcessingInfo processingInfo = result.getProcessingInfo();
		assertThat(processingInfo.getStepName()).isEqualTo("SendEmail");
		assertThat(processingInfo.getStepIndex()).isEqualTo(4);
		assertThat(processingInfo.getPrevProcessingId()).isEqualTo(originalProcessingId);
		assertThat(processingInfo.getTimeStampStart()).isBeforeOrEqualTo(processingInfo.getTimeStampFinish());
	}
	
	@Test
	void sendMailToManyRecipients() {
		//GIVEN
		String INPUT_JSON_FILE = "messages/e_email_message_2_many.json";
		Message message = JsonUtils.readObjectFromClassPathResource(INPUT_JSON_FILE, Message.class);
		
		//WHEN -THEN
		Assertions.assertThatThrownBy(() -> {
			checkPreConditions.andThen(functionSend).apply(message);
		})
		.isInstanceOf(PayloadValidationException.class)
		.hasMessageContaining("Email sender can send to only one recipient. Found more: ");
	}
	
	@Test
	void sendEmailToNonEmailEnpoint() {
		//GIVEN
		String INPUT_JSON_FILE = "messages/e_email_message_2_push_endpoint.json";
		Message message = JsonUtils.readObjectFromClassPathResource(INPUT_JSON_FILE, Message.class);
		
		//WHEN -THEN
		Assertions.assertThatThrownBy(() -> {
			checkPreConditions.andThen(functionSend).apply(message);
		})
		.isInstanceOf(PayloadValidationException.class)
		.hasMessageContaining("Email sender can send to Email endpoints only. Found ");
	}
}
