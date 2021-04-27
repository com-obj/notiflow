package com.obj.nc.repositories;

import com.obj.nc.BaseIntegrationTest;
import com.obj.nc.SystemPropertyActiveProfileResolver;
import com.obj.nc.domain.message.Message;
import com.obj.nc.utils.JsonUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ActiveProfiles(value = "test", resolver = SystemPropertyActiveProfileResolver.class)
@SpringBootTest
public class MessageRepositoryTest extends BaseIntegrationTest {
	
	@Autowired MessageRepository messageRepository;
	
	@BeforeEach
	void setUp(@Autowired JdbcTemplate jdbcTemplate) {
		purgeNotifTables(jdbcTemplate);
	}
	
	@Test
	public void testPersistingSingleMessage() {
		//GIVEN
		String INPUT_JSON_FILE = "messages/email_message.json";
		Message notificationIntent = JsonUtils.readObjectFromClassPathResource(INPUT_JSON_FILE, Message.class);
		notificationIntent.getHeader().setFlowId("default-flow");
		UUID[] eventIds = new UUID[]{UUID.randomUUID(), UUID.randomUUID()};
		notificationIntent.getHeader().setEventIdsAsArray(eventIds);
		
		messageRepository.save(notificationIntent);
		
		Optional<Message> oIntentInDB = messageRepository.findById(notificationIntent.getId());
		
		Assertions.assertThat(oIntentInDB.isPresent()).isTrue();
		Message intentInDB = oIntentInDB.get();
		Assertions.assertThat(intentInDB.getPayloadTypeName()).isEqualTo("MESSAGE"); 
		Assertions.assertThat(intentInDB.getTimeCreated()).isNotNull();
		Assertions.assertThat(intentInDB.getHeader().getFlowId()).isEqualTo("default-flow");
		Assertions.assertThat(intentInDB.getHeader().getEventIdsAsArray()).isEqualTo(eventIds);
		Assertions.assertThat(intentInDB.getBody().toJSONString()).contains("john.doe@objectify.sk");
	}
	
	@Test
	public void testFindByIdInContainingIntentsId() {
		//GIVEN
		String INPUT_JSON_FILE = "messages/email_message.json";
		Message notificationIntent = JsonUtils.readObjectFromClassPathResource(INPUT_JSON_FILE, Message.class);
		notificationIntent.getHeader().setFlowId("default-flow");
		UUID[] eventIds = new UUID[]{UUID.randomUUID(), UUID.randomUUID()};
		notificationIntent.getHeader().setEventIdsAsArray(eventIds);
		notificationIntent.setId(UUID.fromString("bf44aedf-6439-4e7f-a136-3dc78202981b"));
		messageRepository.save(notificationIntent);
		// WHEN
		List<Message> oIntentInDB = messageRepository.findByIdIn(Arrays.asList(
				UUID.fromString("bf44aedf-6439-4e7f-a136-3dc78202981a"),
				UUID.fromString("bf44aedf-6439-4e7f-a136-3dc78202981b")));
		// THEN
		Assertions.assertThat(!oIntentInDB.isEmpty()).isTrue();
	}
	
	@Test
	public void testFindByIdInNotContainingIntentsId() {
		//GIVEN
		String INPUT_JSON_FILE = "messages/email_message.json";
		Message notificationIntent = JsonUtils.readObjectFromClassPathResource(INPUT_JSON_FILE, Message.class);
		notificationIntent.getHeader().setFlowId("default-flow");
		UUID[] eventIds = new UUID[]{UUID.randomUUID(), UUID.randomUUID()};
		notificationIntent.getHeader().setEventIdsAsArray(eventIds);
		notificationIntent.setId(UUID.fromString("bf44aedf-6439-4e7f-a136-3dc78202981b"));
		messageRepository.save(notificationIntent);
		// WHEN
		List<Message> oIntentInDB = messageRepository.findByIdIn(Arrays.asList(
				UUID.fromString("bf44aedf-6439-4e7f-a136-3dc78202981c")));
		// THEN
		Assertions.assertThat(oIntentInDB.isEmpty()).isTrue();
	}
	
}
