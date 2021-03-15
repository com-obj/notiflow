package com.obj.nc;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Optional;

import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.assertj.core.api.Assertions;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;

import com.icegreen.greenmail.util.GreenMailUtil;
import com.obj.nc.BaseIntegrationTest.MailMessageForAssertions;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

/*
    SINGLETON pattern class with containers for all test classes
    see more: https://www.testcontainers.org/test_framework_integration/manual_lifecycle_control/#singleton-containers
 */
@SpringBootTest(properties = "spring.main.allow-bean-definition-overriding=true")
public abstract class BaseIntegrationTest {

    static FixedPortPostgreSQLContainer<?> POSTGRESQL_CONTAINER;
    
    public static final MediaType APPLICATION_JSON_UTF8 = new MediaType(MediaType.APPLICATION_JSON.getType(), MediaType.APPLICATION_JSON.getSubtype(), Charset.forName("utf8"));

    @Data
    @AllArgsConstructor
    @ToString
    public static class MailMessageForAssertions {
    	Optional<String> from;
    	Optional<String> to;
    	Optional<String> subjectPart;
    	String[] textParts;
    	
    	public static MailMessageForAssertions as(String to,String subjectPart,String ... textParts ) {
    		MailMessageForAssertions msg = new MailMessageForAssertions(Optional.empty(),Optional.of(to),Optional.of(subjectPart), textParts);
    		return msg;
    	}
    	
    }
    
    public static void assertMessageCount(MimeMessage[] receivedMessages, String emailAddress, int count) {
    	try { 
	    	int totalCount =0;
	    	for (MimeMessage msg: receivedMessages) {
	    		boolean has = Arrays.stream(msg.getAllRecipients())
	    			.map(adr-> ((InternetAddress)adr).getAddress())
	    			.filter(email -> emailAddress.equals(email))
	    			.findFirst().isPresent();
	    		
	    		if (has) {
	    			totalCount++;
	    		}
	    	}
	    	
	    	Assertions.assertThat(totalCount).isEqualTo(count);
    	} catch (MessagingException e) {
    		throw new RuntimeException(e);
    	}
    }
    
    public static void assertMessagesContains(MimeMessage[] receivedMessages, MailMessageForAssertions msgToMatch) {
		 System.out.println("About to check message TO:" + msgToMatch.getTo() + " SUBJECT:" + msgToMatch.getSubjectPart() + " BODY:" + Arrays.toString(msgToMatch.textParts) );
//		 System.out.println("BODY:" + GreenMailUtil.getBody(message) );
		 
    	try { 
	    	 for (MimeMessage message: receivedMessages) {
	    		 

	    		 
		    	 boolean isMatching = true;
	    		 
	    		 if (msgToMatch.getTo().isPresent()) {
	    			 
						isMatching &=
								 
								 Arrays.stream( message.getAllRecipients())
								 	.map(fromAddr -> ((InternetAddress)fromAddr).getAddress())
								 	.filter(fromAddr -> msgToMatch.getTo().get().equals(fromAddr) )
								 	.findFirst().isPresent(); 
	    		 }
	    		 
	    		 if (msgToMatch.getSubjectPart().isPresent()) {
	    			 
						isMatching &= message.getSubject().contains(msgToMatch.getSubjectPart().get());
		
	    		 }
	    		 
	    		 for (String bodyTextToMatch: msgToMatch.getTextParts()) {
	    			 	
						isMatching &= GreenMailUtil.getBody(message).contains(bodyTextToMatch);
		
	    		 }
	    		 
	    		 if (isMatching) {
	    			 return;
	    		 }
	    	 }	
	    	 
	    	 
	    	 
	    	 Assertions.assertThat(false).as("Greenmail didn't recieve mail which would match to " + msgToMatch.toString()).isTrue();
    	} catch (MessagingException e) {
    		throw new RuntimeException(e);
    	}
	}
}
