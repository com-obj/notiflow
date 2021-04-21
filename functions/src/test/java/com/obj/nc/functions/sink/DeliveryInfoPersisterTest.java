package com.obj.nc.functions.sink;

import static org.hamcrest.MatcherAssert.assertThat;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.assertj.core.api.Assertions;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import com.obj.nc.BaseIntegrationTest;
import com.obj.nc.SystemPropertyActiveProfileResolver;
import com.obj.nc.domain.endpoints.EmailEndpoint;
import com.obj.nc.domain.endpoints.RecievingEndpoint;
import com.obj.nc.domain.message.Message;
import com.obj.nc.domain.notifIntent.NotificationIntent;
import com.obj.nc.functions.processors.deliveryInfo.DeliveryInfoSendGenerator;
import com.obj.nc.functions.processors.deliveryInfo.DeliveryInfoProcessingGenerator;
import com.obj.nc.functions.processors.dummy.DummyRecepientsEnrichmentProcessingFunction;
import com.obj.nc.functions.processors.eventIdGenerator.GenerateEventIdProcessingFunction;
import com.obj.nc.functions.processors.messageBuilder.MessagesFromNotificationIntentProcessingFunction;
import com.obj.nc.functions.processors.senders.dtos.DeliveryInfoSendResult;
import com.obj.nc.functions.sink.deliveryInfoPersister.DeliveryInfoPersister;
import com.obj.nc.functions.sink.deliveryInfoPersister.domain.DeliveryInfo;
import com.obj.nc.functions.sink.deliveryInfoPersister.domain.DeliveryInfo.DELIVERY_STATUS;
import com.obj.nc.repositories.DeliveryInfoRepository;
import com.obj.nc.utils.JsonUtils;

@ActiveProfiles(value = "test", resolver = SystemPropertyActiveProfileResolver.class)
@SpringBootTest
public class DeliveryInfoPersisterTest extends BaseIntegrationTest {
	
	@Autowired private GenerateEventIdProcessingFunction generateEventId;
    @Autowired private DummyRecepientsEnrichmentProcessingFunction resolveRecipients;
    @Autowired private DeliveryInfoPersister deliveryInfoPersister;
    @Autowired private DeliveryInfoSendGenerator deliveryInfoSendGenerator;
    @Autowired private DeliveryInfoProcessingGenerator deliveryInfoProcessingGenerator;
    @Autowired private MessagesFromNotificationIntentProcessingFunction generateMessagesFromIntent;
    @Autowired private DeliveryInfoRepository deliveryInfoRepo;
    @Autowired private JdbcTemplate jdbcTemplate;
	
    @BeforeEach
    void setUp(@Autowired JdbcTemplate jdbcTemplate) {
    	purgeNotifTables(jdbcTemplate);
    }

    @Test
    void testPersistPIForEventWithRecipients() {
        // GIVEN
        String INPUT_JSON_FILE = "events/ba_job_post.json";
        NotificationIntent notificationIntent = JsonUtils.readObjectFromClassPathResource(INPUT_JSON_FILE, NotificationIntent.class);

        notificationIntent = (NotificationIntent)generateEventId.apply(notificationIntent);
        UUID eventId = notificationIntent.getHeader().getEventIds().get(0);
        notificationIntent = resolveRecipients.apply(notificationIntent);
        
        //WHEN
        List<DeliveryInfoSendResult> infos = deliveryInfoProcessingGenerator.apply(notificationIntent);
        infos.forEach(delivery -> {
        	//processing delivery infos
        	deliveryInfoPersister.accept(delivery);
        });
        
        //THEN check processing deliveryInfo
        assertEnpointPersistedNotDuplicated(notificationIntent);
        List<DeliveryInfo> deliveryInfos = deliveryInfoRepo.findByEventIdOrderByProcessedOn(eventId);
        
        Assertions.assertThat(deliveryInfos.size()).isEqualTo(3);
        deliveryInfos.forEach(info -> {
        	Assertions.assertThat(info.getStatus()).isEqualTo(DELIVERY_STATUS.PROCESSING);
        	Assertions.assertThat(info.getProcessedOn()).isNotNull();
        	Assertions.assertThat(info.getEndpointId()).isIn("john.doe@objectify.sk","john.dudly@objectify.sk", "all@objectify.sk");
        });
        
        
        //WHEN
        List<Message> messages = generateMessagesFromIntent.apply(notificationIntent);
        
        messages.forEach(msg -> {
        	//pretend delivery
        	List<DeliveryInfoSendResult> deliveredInfos = deliveryInfoSendGenerator.apply(msg);
        	
        	//delivered delivery infos
        	deliveredInfos.forEach(delivery -> {
            	deliveryInfoPersister.accept(delivery);
            });
        });
        
        //THEN check delivered deliveryInfo
        assertEnpointPersistedNotDuplicated(notificationIntent);
        deliveryInfos = deliveryInfoRepo.findByEventIdOrderByProcessedOn(eventId);
        
        Assertions.assertThat(deliveryInfos.size()).isEqualTo(6);
        List<DeliveryInfo> deliveredInfos = deliveryInfos.stream().filter(info -> info.getStatus() == DELIVERY_STATUS.SEND).collect(Collectors.toList());
        
        Assertions.assertThat(deliveredInfos.size()).isEqualTo(3);
        deliveryInfos.forEach(info -> {
        	Assertions.assertThat(info.getProcessedOn()).isNotNull();
        	Assertions.assertThat(info.getEndpointId()).isIn("john.doe@objectify.sk","john.dudly@objectify.sk", "all@objectify.sk");
        });
    }


	private void assertEnpointPersistedNotDuplicated(NotificationIntent notificationIntent) {
		List<Map<String, Object>> persistedEndpoints = jdbcTemplate.queryForList("select * from nc_endpoint");
        assertThat(persistedEndpoints, CoreMatchers.notNullValue());
        Assertions.assertThat(persistedEndpoints.size()).isEqualTo(3);

        for (int i = 0; i < persistedEndpoints.size(); i++) {
            List<RecievingEndpoint> recievingEndpoints = notificationIntent.getBody().getRecievingEndpoints();
            assertThat(persistedEndpoints.get(i).get("endpoint_id"), CoreMatchers.equalTo(((EmailEndpoint) recievingEndpoints.get(i)).getEmail()));
            assertThat(persistedEndpoints.get(i).get("endpoint_type"), CoreMatchers.equalTo(recievingEndpoints.get(i).getEndpointType()));
        }
	}

}
