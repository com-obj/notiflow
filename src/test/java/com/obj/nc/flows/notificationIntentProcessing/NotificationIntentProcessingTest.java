package com.obj.nc.flows.notificationIntentProcessing;

import java.time.Duration;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import com.obj.nc.domain.notifIntent.NotificationIntent;
import com.obj.nc.flows.intenToMessageToSender.NotificationIntentProcessingFlow;
import com.obj.nc.functions.processors.eventIdGenerator.GenerateEventIdProcessingFunction;
import com.obj.nc.repositories.DeliveryInfoRepository;
import com.obj.nc.testUtils.BaseIntegrationTest;
import com.obj.nc.testUtils.SystemPropertyActiveProfileResolver;
import com.obj.nc.utils.JsonUtils;

@ActiveProfiles(value = "test", resolver = SystemPropertyActiveProfileResolver.class)
@SpringBootTest
public class NotificationIntentProcessingTest extends BaseIntegrationTest {

	@Autowired private NotificationIntentProcessingFlow intentFlow; 
	
	@Autowired private GenerateEventIdProcessingFunction generateEventId;
    @Autowired private DeliveryInfoRepository deliveryInfoRepo;

    @BeforeEach
    void setUp(@Autowired JdbcTemplate jdbcTemplate) {
    	purgeNotifTables(jdbcTemplate);
    }
    
    @Test
    void testResolveRecipientsMergeWithExisting() {
        // given
        String INPUT_JSON_FILE = "intents/ba_job_post_recipients.json";
        NotificationIntent notificationIntent = JsonUtils.readObjectFromClassPathResource(INPUT_JSON_FILE, NotificationIntent.class);
        notificationIntent = (NotificationIntent)generateEventId.apply(notificationIntent);
        UUID eventId = notificationIntent.getHeader().getEventIds().get(0);

        // when
        intentFlow.processNotificationIntent(notificationIntent);

        //THEN check processing deliveryInfo
        awaitSent(eventId, 3, Duration.ofSeconds(5));
    }

}
