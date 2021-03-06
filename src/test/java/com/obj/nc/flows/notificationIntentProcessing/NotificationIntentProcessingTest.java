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

package com.obj.nc.flows.notificationIntentProcessing;

import com.icegreen.greenmail.configuration.GreenMailConfiguration;
import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.ServerSetupTest;
import com.obj.nc.domain.endpoints.EmailEndpoint;
import com.obj.nc.domain.event.GenericEvent;
import com.obj.nc.domain.notifIntent.NotificationIntent;
import com.obj.nc.flows.intenProcessing.NotificationIntentProcessingFlow;
import com.obj.nc.repositories.GenericEventRepository;
import com.obj.nc.repositories.GenericEventRepositoryTest;
import com.obj.nc.testUtils.BaseIntegrationTest;
import com.obj.nc.testUtils.SystemPropertyActiveProfileResolver;
import com.obj.nc.utils.JsonUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.integration.endpoint.SourcePollingChannelAdapter;
import org.springframework.integration.test.context.SpringIntegrationTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.time.Duration;
import java.util.UUID;

import static com.obj.nc.flows.inputEventRouting.config.InputEventRoutingFlowConfig.GENERIC_EVENT_CHANNEL_ADAPTER_BEAN_NAME;

@ActiveProfiles(value = "test", resolver = SystemPropertyActiveProfileResolver.class)
@SpringIntegrationTest(noAutoStartup = GENERIC_EVENT_CHANNEL_ADAPTER_BEAN_NAME)
@SpringBootTest(properties = {
    "nc.contacts-store.jsonStorePathAndFileName=src/test/resources/contact-store/contact-store.json", 
})
public class NotificationIntentProcessingTest extends BaseIntegrationTest {

	@Autowired private NotificationIntentProcessingFlow intentFlow; 
	@Autowired private GenericEventRepository eventRepo;
	
	@Qualifier(GENERIC_EVENT_CHANNEL_ADAPTER_BEAN_NAME)
	@Autowired private SourcePollingChannelAdapter pollAbleSource;

    @BeforeEach
    void setUp(@Autowired JdbcTemplate jdbcTemplate) {
    	purgeNotifTables(jdbcTemplate);
    	
    	pollAbleSource.start();
    }

    @Test
    void testSimpleIntentToMessage() {
        // given		
        NotificationIntent notificationIntent = NotificationIntent.createWithStaticContent(
            "subject", 
            "body"
        );
        notificationIntent.addRecipientsByIds(UUID.fromString("baf25cbe-2975-4666-adda-c8ea01dc909d")); //John Doe

        // when
        intentFlow.processNotificationIntent(notificationIntent);

        //THEN check processing deliveryInfo
        awaitSentForIntent(notificationIntent.getId(), 1, Duration.ofSeconds(5));
    }
    
    @Test
    void testResolveRecipientsMergeWithExisting() {
        // given
		GenericEvent event = GenericEventRepositoryTest.createProcessedEvent();
		UUID eventId = eventRepo.save(event).getId();
		
        // given		
        NotificationIntent notificationIntent = NotificationIntent.createWithStaticContent(
            "Business Intelligence (BI) Developer", 
            "We are looking for a Business Intelligence"
        );        
        notificationIntent.addRecipientsByName(
            "John Doe",
            "John Dudly",
            "Objectify"
        );
        notificationIntent.addPreviousEventId(eventId);

        // when
        intentFlow.processNotificationIntent(notificationIntent);

        //THEN check processing deliveryInfo
        awaitSent(eventId, 3, Duration.ofSeconds(10));
    }
    
    @AfterEach
    public void stopSourcePolling() {
    	pollAbleSource.stop();
    }
    
    @RegisterExtension
    protected static GreenMailExtension greenMail = new GreenMailExtension(ServerSetupTest.SMTP)
      	.withConfiguration(
      			GreenMailConfiguration.aConfig()
      			.withUser("no-reply@objectify.sk", "xxx"))
      	.withPerMethodLifecycle(true);

}
