package com.obj.nc.flows.messageProcessing;

import java.time.Duration;
import java.util.UUID;

import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import com.obj.nc.domain.message.EmailMessage;
import com.obj.nc.repositories.DeliveryInfoRepository;
import com.obj.nc.testUtils.BaseIntegrationTest;
import com.obj.nc.testUtils.SystemPropertyActiveProfileResolver;
import com.obj.nc.utils.JsonUtils;

@ActiveProfiles(value = "test", resolver = SystemPropertyActiveProfileResolver.class)
@SpringBootTest
public class MessageProcessingTest extends BaseIntegrationTest {

	@Autowired private MessageProcessingFlow msgFlow; 
	
    @Autowired private DeliveryInfoRepository deliveryInfoRepo;
 	
    @BeforeEach
    void setUp(@Autowired JdbcTemplate jdbcTemplate) {
    	purgeNotifTables(jdbcTemplate);
    }
    
    @Test
    void testResolveRecipientsMergeWithExisting() {
        // given
        String INPUT_JSON_FILE = "messages/e_email_message_2_many.json";
        EmailMessage msg = JsonUtils.readObjectFromClassPathResource(INPUT_JSON_FILE, EmailMessage.class);
        UUID eventId = msg.getHeader().getEventIds().get(0);

        // when
        msgFlow.processMessage(msg);

        //THEN check processing deliveryInfo
        Awaitility.await().atMost(Duration.ofSeconds(3)).until(() -> deliveryInfoRepo.findByEventIdOrderByProcessedOn(eventId).size()==2);        
    }


}
