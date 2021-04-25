package com.obj.nc.repositories;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.obj.nc.BaseIntegrationTest;
import com.obj.nc.SystemPropertyActiveProfileResolver;
import com.obj.nc.domain.notifIntent.NotificationIntent;
import com.obj.nc.utils.JsonUtils;

@ActiveProfiles(value = "test", resolver = SystemPropertyActiveProfileResolver.class)
@SpringBootTest
public class NotificationIntentRepositoryTest extends BaseIntegrationTest {

	
	@Autowired NotificationIntentRepository intentRepository;
	
	@Test
	public void testPersistingSingleEvent() {
		//GIVEN
		 String INPUT_JSON_FILE = "events/ba_job_post.json";
	     NotificationIntent notificationIntent = JsonUtils.readObjectFromClassPathResource(INPUT_JSON_FILE, NotificationIntent.class);
	     notificationIntent.getHeader().setFlowId("default-flow");
	     UUID[] eventIds = new UUID[]{UUID.randomUUID(), UUID.randomUUID()};
	     notificationIntent.getHeader().setEventIdsAsArray(eventIds);
	     
	     notificationIntent = intentRepository.save(notificationIntent);
	     
	     Optional<NotificationIntent> oIntentInDB = intentRepository.findById(notificationIntent.getId());
	     
	     Assertions.assertThat(oIntentInDB.isPresent()).isTrue();
	     NotificationIntent intentInDB = oIntentInDB.get();
	     Assertions.assertThat(intentInDB.getPayloadTypeName()).isEqualTo("EVENT"); 
	     Assertions.assertThat(intentInDB.getTimeCreated()).isNotNull();
	     Assertions.assertThat(intentInDB.getHeader().getFlowId()).isEqualTo("default-flow");
	     Assertions.assertThat(intentInDB.getHeader().getEventIdsAsArray()).isEqualTo(eventIds);
	     Assertions.assertThat(intentInDB.getBody().toJSONString()).contains("Business Intelligence (BI) Developer");
	}
}
