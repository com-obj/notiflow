package com.obj.nc.repositories;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import com.obj.nc.BaseIntegrationTest;
import com.obj.nc.SystemPropertyActiveProfileResolver;
import com.obj.nc.domain.notifIntent.NotificationIntent;
import com.obj.nc.utils.JsonUtils;

@ActiveProfiles(value = "test", resolver = SystemPropertyActiveProfileResolver.class)
@SpringBootTest
public class NotificationIntentRepositoryTest extends BaseIntegrationTest {
	
	@Autowired NotificationIntentRepository intentRepository;
	
	@BeforeEach
	void setUp(@Autowired JdbcTemplate jdbcTemplate) {
		purgeNotifTables(jdbcTemplate);
	}
	
	@Test
	public void testPersistingSingleIntent() {
		//GIVEN
		 String INPUT_JSON_FILE = "intents/ba_job_post.json";
	     NotificationIntent notificationIntent = JsonUtils.readObjectFromClassPathResource(INPUT_JSON_FILE, NotificationIntent.class);
	     notificationIntent.getHeader().setFlowId("default-flow");
	     UUID[] eventIds = new UUID[]{UUID.randomUUID(), UUID.randomUUID()};
	     notificationIntent.getHeader().setEventIdsAsArray(eventIds);
 		
	     intentRepository.save(notificationIntent);
	     
	     Optional<NotificationIntent> oIntentInDB = intentRepository.findById(notificationIntent.getId());
	     
	     Assertions.assertThat(oIntentInDB.isPresent()).isTrue();
	     NotificationIntent intentInDB = oIntentInDB.get();
	     Assertions.assertThat(intentInDB.getPayloadTypeName()).isEqualTo("INTENT"); 
	     Assertions.assertThat(intentInDB.getTimeCreated()).isNotNull();
	     Assertions.assertThat(intentInDB.getHeader().getFlowId()).isEqualTo("default-flow");
	     Assertions.assertThat(intentInDB.getHeader().getEventIdsAsArray()).isEqualTo(eventIds);
	     Assertions.assertThat(intentInDB.getBody().getSubject()).contains("Business Intelligence (BI) Developer");
	     Assertions.assertThat(intentInDB.getBody().getBody()).contains("We are looking for a Business Intelligence (BI) Developer to create...");	    
	}
	
	@Test
	public void testFindByIdInContainingIntentsId() {
		//GIVEN
		String INPUT_JSON_FILE = "intents/ba_job_post.json";
		NotificationIntent notificationIntent = JsonUtils.readObjectFromClassPathResource(INPUT_JSON_FILE, NotificationIntent.class);
		notificationIntent.getHeader().setFlowId("default-flow");
		UUID[] eventIds = new UUID[]{UUID.randomUUID(), UUID.randomUUID()};
		notificationIntent.getHeader().setEventIdsAsArray(eventIds);
		notificationIntent.setId(UUID.fromString("bf44aedf-6439-4e7f-a136-3dc78202981b"));
		intentRepository.save(notificationIntent);
		// WHEN
		List<NotificationIntent> oIntentInDB = intentRepository.findByIdIn(Arrays.asList(
				UUID.fromString("bf44aedf-6439-4e7f-a136-3dc78202981a"),
				UUID.fromString("bf44aedf-6439-4e7f-a136-3dc78202981b")));
		// THEN
		Assertions.assertThat(!oIntentInDB.isEmpty()).isTrue();
	}
	
	@Test
	public void testFindByIdInNotContainingIntentsId() {
		//GIVEN
		String INPUT_JSON_FILE = "intents/ba_job_post.json";
		NotificationIntent notificationIntent = JsonUtils.readObjectFromClassPathResource(INPUT_JSON_FILE, NotificationIntent.class);
		notificationIntent.getHeader().setFlowId("default-flow");
		UUID[] eventIds = new UUID[]{UUID.randomUUID(), UUID.randomUUID()};
		notificationIntent.getHeader().setEventIdsAsArray(eventIds);
		notificationIntent.setId(UUID.fromString("bf44aedf-6439-4e7f-a136-3dc78202981b"));
		intentRepository.save(notificationIntent);
		// WHEN
		List<NotificationIntent> oIntentInDB = intentRepository.findByIdIn(Arrays.asList(
				UUID.fromString("bf44aedf-6439-4e7f-a136-3dc78202981c")));
		// THEN
		Assertions.assertThat(oIntentInDB.isEmpty()).isTrue();
	}
	
}
