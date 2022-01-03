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
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.awaitility.Awaitility;
import org.junit.AfterClass;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.slf4j.MDC;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.http.MediaType;
import org.springframework.integration.endpoint.SourcePollingChannelAdapter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import static com.obj.nc.flows.inputEventRouting.config.InputEventRoutingFlowConfig.GENERIC_EVENT_CHANNEL_ADAPTER_BEAN_NAME;


import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.nio.charset.Charset;
import java.time.Duration;
import java.util.*;

@Slf4j
public abstract class BaseIntegrationTest implements ApplicationContextAware {

    public static final MediaType APPLICATION_JSON_UTF8 = new MediaType(MediaType.APPLICATION_JSON.getType(), MediaType.APPLICATION_JSON.getSubtype(), Charset.forName("utf8"));
    
    @Autowired Get get;
    @Autowired(required = false) //not all tests require, some might not be @SpringBootTests 
    protected DeliveryInfoRepository deliveryInfoRepo;

    @Autowired ThreadPoolTaskScheduler taskScheduler; 

    public static volatile String testName;
    public static final String MDC_FOR_TESTS_NAME = "testName";

    @BeforeEach
    void startLogging() {
        BaseIntegrationTest.testName = this.getClass().getSimpleName();
        MDC.put(MDC_FOR_TESTS_NAME, testName);

        log.info("TEST START {}", this.getClass().getSimpleName());  
    }

    @AfterEach
    void stopLogging() {    
        log.info("TEST FINISH {}", this.getClass().getSimpleName()); 

        BaseIntegrationTest.testName = null;
        MDC.put(MDC_FOR_TESTS_NAME, testName);
    }

	@AfterClass
    static void cleanUp() {    
        Get.getBean(GENERIC_EVENT_CHANNEL_ADAPTER_BEAN_NAME, SourcePollingChannelAdapter.class).stop();
    }

    public static void purgeNotifTables(@Autowired JdbcTemplate jdbcTemplate) {
        log.info("Purging all tables in test");

        jdbcTemplate.execute("truncate nc_processing_info, " +
				"nc_message_2_endpoint_rel," +
				"nc_delivery_info, " + 
				"nc_endpoint," +
				"nc_event," +
				"nc_intent," +
				"nc_message," +
				"nc_failed_payload," +
				"nc_pulled_notif_data cascade");

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
    		return new MailMessageForAssertions(Optional.empty(),Optional.of(to),Optional.of(subjectPart), textParts);
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
	    			.anyMatch(emailAddress::equals);
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
    
    public static void assertMessagesContains(MimeMessage[] receivedMessages, MailMessageForAssertions msgToMatch) {
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
								 	.anyMatch(fromAddr -> msgToMatch.getTo().get().equals(fromAddr) );
	    		 }

	    		 if (msgToMatch.getSubjectPart().isPresent()) {
	    			 	System.out.println("Checking subject '" + message.getSubject() + "' to contain '" + msgToMatch.getSubjectPart().get() + "'");
	    			 	isMatchingSubject &= message.getSubject().contains(msgToMatch.getSubjectPart().get());

	    		 }

	    		 for (String bodyTextToMatch: msgToMatch.getTextParts()) {

	    			 isMatchingContent &= GreenMailUtil.getBody(message).contains(bodyTextToMatch);

	    		 }

	    		 if (isMatchingTo && isMatchingSubject && isMatchingContent) {
	    			 return;
	    		 }
	    	 }

	    	 Assertions.assertThat(false).as("Greenmail didn't receive mail which would match to " + msgToMatch.toString()).isTrue();
    	} catch (MessagingException e) {
    		throw new RuntimeException(e);
    	}
	}
    
    //In most cases it is overkill to wait for sent,.. but otherwise test finish before execution finishes resulting in shutdown exceptions (tests are green)
    public void awaitSent(UUID eventId, int messageCount, Duration maxTime) {
    	
    	Awaitility.await().atMost(maxTime).until(() -> {
    		List<DeliveryInfo> infos = deliveryInfoRepo.findByEventIdAndStatusOrderByProcessedOn(eventId, DELIVERY_STATUS.SENT);
    		log.info("DeliveryInfos: {}",infos);
    		
    		return infos.size()==messageCount;
    	});    
    }
    
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    	Get.setApplicationContext(applicationContext);
    }

	public void wiatForIntegrationFlowsToFinish(int numberOfSeconds) {
		Awaitility.await().atMost(Duration.ofSeconds(numberOfSeconds)).until(() -> taskScheduler.getActiveCount()==0);   		
	}

}
