package com.obj.nc.functions.processors.senders;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.ActiveProfiles;

import com.icegreen.greenmail.util.GreenMail;
import com.obj.nc.domain.message.Message;
import com.obj.nc.functions.processors.senders.EmailSenderSink.SendMessage;
import com.obj.nc.utils.GreenMailManager;
import com.obj.nc.utils.JsonUtils;

@SpringBootTest(webEnvironment = WebEnvironment.NONE)
@ActiveProfiles(profiles = "test")
class EmailSenderSinkTest {
	
	@Autowired 
	private GreenMailManager greenMailManager;
	
	@Autowired 
	private SendMessage functionSend;


	@Test
	void test() throws MessagingException, IOException {
		//GIVEN
		String INPUT_JSON_FILE = "messages/email_message.json";
		Message message = JsonUtils.readObjectFromClassPathResource(INPUT_JSON_FILE, Message.class);
		
		//WHEN
		Message result = functionSend.apply(message);
		
		//THEN		 
		GreenMail gm = greenMailManager.getGreenMail();
		MimeMessage[] messages = gm.getReceivedMessages();
		assertThat( messages.length ).isEqualTo(1);
		assertThat( messages[0].getSubject() ).isEqualTo("Subject");
	}
}
