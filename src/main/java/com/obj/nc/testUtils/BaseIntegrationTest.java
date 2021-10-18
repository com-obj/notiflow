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

package com.obj.nc.testUtils;

import com.icegreen.greenmail.util.GreenMailUtil;
import com.obj.nc.Get;
import com.obj.nc.functions.processors.deliveryInfo.domain.DeliveryInfo;
import com.obj.nc.functions.processors.deliveryInfo.domain.DeliveryInfo.DELIVERY_STATUS;
import com.obj.nc.repositories.DeliveryInfoRepository;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;
import lombok.extern.log4j.Log4j2;
import org.assertj.core.api.Assertions;
import org.awaitility.Awaitility;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import java.nio.charset.Charset;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Log4j2
public abstract class BaseIntegrationTest implements ApplicationContextAware {

    public static final MediaType APPLICATION_JSON_UTF8 = new MediaType(MediaType.APPLICATION_JSON.getType(), MediaType.APPLICATION_JSON.getSubtype(), Charset.forName("utf8"));
    
    @Autowired Get get;
    @Autowired(required = false) //not all tests require, some might not be @SpringBootTests 
    DeliveryInfoRepository deliveryInfoRepo;
    @Autowired ThreadPoolTaskScheduler executor;

    
    public static void purgeNotifTables(@Autowired JdbcTemplate jdbcTemplate) {
        log.info("Purging all tables in test");
        
        jdbcTemplate.batchUpdate("delete from nc_processing_info");
        jdbcTemplate.batchUpdate("delete from nc_delivery_info");
        jdbcTemplate.batchUpdate("delete from nc_endpoint");        
        jdbcTemplate.batchUpdate("delete from nc_event");    
        jdbcTemplate.batchUpdate("delete from nc_intent");  
        jdbcTemplate.batchUpdate("delete from nc_message");  
        jdbcTemplate.batchUpdate("delete from nc_failed_payload");
    }
    
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
    
	public static void assertMessagesSendTo(MimeMessage[] messages, String emailAddress, int expectedCount) {
		long count = Arrays.stream(messages)
        	.flatMap(m-> {
				try {
					return Arrays.stream(m.getAllRecipients());
				} catch (MessagingException e) {
					throw new RuntimeException(e);
				}
			})
        	.map(r -> (InternetAddress)r)
        	.filter(ia -> ia.getAddress().equals(emailAddress)).count();
    	 Assertions.assertThat( count ).isEqualTo(expectedCount);
	}
    
    public static List<MimeMessage> assertMessageCount(MimeMessage[] receivedMessages, String emailAddress, int count) {
    	try { 
    		List<MimeMessage> matched = new ArrayList<>();
    		
	    	int totalCount =0;
	    	for (MimeMessage msg: receivedMessages) {
	    		boolean has = Arrays.stream(msg.getAllRecipients())
	    			.map(adr-> ((InternetAddress)adr).getAddress())
	    			.filter(email -> emailAddress.equals(email))
	    			.findFirst().isPresent();
	    		
	    		if (has) {
	    			matched.add(msg);
	    			totalCount++;
	    		}
	    	}
	    	
	    	Assertions.assertThat(totalCount).isEqualTo(count);
	    	
	    	return matched;
    	} catch (MessagingException e) {
    		throw new RuntimeException(e);
    	}
    }
    
    public static MimeMessage assertMessagesContains(MimeMessage[] receivedMessages, MailMessageForAssertions msgToMatch) {
		 System.out.println("About to check message TO:" + msgToMatch.getTo() + " SUBJECT:" + msgToMatch.getSubjectPart() + " BODY:" + Arrays.toString(msgToMatch.textParts) );
		 
    	try { 
	    	 for (MimeMessage message: receivedMessages) {	    		 
		    	 boolean isMatchingTo = true;
		    	 boolean isMatchingSubject = true;
		    	 boolean isMatchingContent = true;
	    		 
	    		 if (msgToMatch.getTo().isPresent()) {
	    			 
	    			 isMatchingTo &=
								 
								 Arrays.stream( message.getAllRecipients())
								 	.map(fromAddr -> ((InternetAddress)fromAddr).getAddress())
								 	.filter(fromAddr -> msgToMatch.getTo().get().equals(fromAddr) )
								 	.findFirst().isPresent(); 
	    		 }
	    		 
	    		 if (msgToMatch.getSubjectPart().isPresent()) {
	    			 	System.out.println("Checking subject '" + message.getSubject() + "' to contain '" + msgToMatch.getSubjectPart().get() + "'");
	    			 	isMatchingSubject &= message.getSubject().contains(msgToMatch.getSubjectPart().get());
		
	    		 }
	    		 
	    		 for (String bodyTextToMatch: msgToMatch.getTextParts()) {
	    			 	
	    			 isMatchingContent &= GreenMailUtil.getBody(message).contains(bodyTextToMatch);
						
	    		 }
	    		 
	    		 if (isMatchingTo && isMatchingSubject && isMatchingContent) {
	    			 return message;
	    		 }
	    	 }	
	    	 
	    	 Assertions.assertThat(false).as("Greenmail didn't receive mail which would match to " + msgToMatch.toString()).isTrue();
    	} catch (MessagingException e) {
    		throw new RuntimeException(e);
    	}
    	
    	return null;
	}
    
    //In most cases it is overkill to wait for sent,.. but otherwise test finish before execution finishes resulting in shutdown exceptions (tests are green)
    public void awaitSent(UUID eventId, int messageCount, Duration maxTime) {
    	
    	Awaitility.await().atMost(maxTime).until(() -> {
    		List<DeliveryInfo> infos = deliveryInfoRepo.findByEventIdAndStatusOrderByProcessedOn(eventId, DELIVERY_STATUS.SENT);
    		log.info(infos);
    		
    		return infos.size()==messageCount;
    	});    
    }
    
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    	Get.setApplicationContext(applicationContext);
    }

	public void wiatForIntegrationFlowsToFinish(int numberOfSeconds) {
		Awaitility.await().atMost(Duration.ofSeconds(numberOfSeconds)).until(() -> executor.getActiveCount()==0);   		
	}

}
