package com.obj.nc.utils;

import java.io.IOException;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import com.icegreen.greenmail.util.ServerSetupTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.icegreen.greenmail.store.FolderException;
import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.GreenMailUtil;
import com.icegreen.greenmail.util.ServerSetup;

import lombok.Data;
import lombok.extern.log4j.Log4j2;

@Component
@Profile({"dev","test"})
@Log4j2
@Data
public class GreenMailManager  {

    private GreenMail greenMail;

	@Value("${spring.mail.port}")
	private Integer portNumber;

	@Value("${spring.mail.username}")
	private String senderUser;

	@Value("${spring.mail.password}")
	private String senderPasswd;

	@EventListener
    public void onApplicationEvent(ContextRefreshedEvent event) {
		if (greenMail!=null) {
			return;
		}
        greenMail = new GreenMail(ServerSetupTest.ALL);
        greenMail.setUser(senderUser, senderPasswd);
        greenMail.start();

        log.info("Greenmail server started on port {}", portNumber);
    }
	
	@EventListener
    public void onApplicationEvent(ContextClosedEvent event) {
		greenMail.stop();
		greenMail = null;
    }
	
	@Component
	@Profile({"dev","test"})
	public class GreenMailMessageLogger {
		
		@Scheduled(fixedDelay = 5000)
		public void logRecievedMessages() throws FolderException, MessagingException, IOException {
			boolean hasMessages = greenMail.getReceivedMessages().length>0;
			for (MimeMessage msg: greenMail.getReceivedMessages()) {
				
				Object message = msg.getContent();
				log.info("{} {} {}", msg.getAllRecipients(), msg.getSubject(), message);
			}
			
			if (hasMessages) {
				greenMail.purgeEmailFromAllMailboxes();
			}
		}
	}
	
}
