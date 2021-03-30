package com.obj.nc;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.binder.test.InputDestination;
import org.springframework.cloud.stream.binder.test.OutputDestination;
import org.springframework.cloud.stream.binder.test.TestChannelBinderConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.converter.CompositeMessageConverter;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import com.obj.nc.domain.message.Message;
import com.obj.nc.domain.notifIntent.NotificationIntent;
import com.obj.nc.utils.JsonUtils;

@ActiveProfiles(value = "test", resolver = SystemPropertyActiveProfileResolver.class)
@Import(TestChannelBinderConfiguration.class)
@ContextConfiguration(classes = EventGeneratorTestApplication.class)
public class IntegrationTests extends BaseIntegrationTest {

	private static final String FINAL_STEP_QUEUE_NAME = "send-message.destination";

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private InputDestination source;

	@Autowired
	private OutputDestination target;

	@Autowired
	private CompositeMessageConverter messageConverter;

	@Test
	public void testEventEmited() {
		String INPUT_JSON_FILE = "allEvents/ba_job_post.json";
		NotificationIntent notificationIntent = JsonUtils.readObjectFromClassPathResource(INPUT_JSON_FILE, NotificationIntent.class);
		org.springframework.messaging.Message<NotificationIntent> inputNotificationIntent = convertBeanToMessagePayload(notificationIntent);

		source.send(inputNotificationIntent);

		org.springframework.messaging.Message<byte[]> payload1 = target.receive(3000,FINAL_STEP_QUEUE_NAME);
		Message message1 = (Message) messageConverter.fromMessage(payload1, Message.class);
		org.springframework.messaging.Message<byte[]> payload2 = target.receive(3000,FINAL_STEP_QUEUE_NAME);
		Message message2 = (Message) messageConverter.fromMessage(payload2, Message.class);
		org.springframework.messaging.Message<byte[]> payload3 = target.receive(3000,FINAL_STEP_QUEUE_NAME);
		Message message3 = (Message) messageConverter.fromMessage(payload3, Message.class);


		MatcherAssert.assertThat(message1, CoreMatchers.notNullValue());
		MatcherAssert.assertThat(message2, CoreMatchers.notNullValue());
		MatcherAssert.assertThat(message3, CoreMatchers.notNullValue());
	}

	@Test
	public void testProcessJournalingPersisted() {
		// given empty processing info table
		jdbcTemplate.execute("truncate table nc_processing_info");

		// and event
		String INPUT_JSON_FILE = "allEvents/ba_job_post.json";
		NotificationIntent notificationIntent = JsonUtils.readObjectFromClassPathResource(INPUT_JSON_FILE, NotificationIntent.class);
		org.springframework.messaging.Message<NotificationIntent> inputNotificationIntent = convertBeanToMessagePayload(notificationIntent);

		// when event processed
		source.send(inputNotificationIntent);

		org.springframework.messaging.Message<byte[]> payload1 = target.receive(3000,FINAL_STEP_QUEUE_NAME);
		Message message1 = (Message) messageConverter.fromMessage(payload1, Message.class);
		org.springframework.messaging.Message<byte[]> payload2 = target.receive(3000,FINAL_STEP_QUEUE_NAME);
		Message message2 = (Message) messageConverter.fromMessage(payload2, Message.class);
		org.springframework.messaging.Message<byte[]> payload3 = target.receive(3000,FINAL_STEP_QUEUE_NAME);
		Message message3 = (Message) messageConverter.fromMessage(payload3, Message.class);

		// then process should be journaled in database
		List<Map<String, Object>> journaledRows = jdbcTemplate.queryForList("select payload_type, payload_id, step_name from nc_processing_info");

		Map<String, Object> step0 = journaledRows.get(0);
		MatcherAssert.assertThat(step0.get("payload_type"), CoreMatchers.equalTo("EVENT"));
		MatcherAssert.assertThat(step0.get("step_name"), CoreMatchers.equalTo("ValidateAndGenerateEventId"));

		Map<String, Object> step1 = journaledRows.get(1);
		MatcherAssert.assertThat(step1.get("payload_type"), CoreMatchers.equalTo("EVENT"));
		MatcherAssert.assertThat(step1.get("step_name"), CoreMatchers.equalTo("FindRecepientsUsingKoderiaSubsription"));

		Map<String, Object> step2 = journaledRows.get(2);
		MatcherAssert.assertThat(step2.get("payload_id"), CoreMatchers.equalTo(message1.getHeader().getId()));
		MatcherAssert.assertThat(step2.get("step_name"), CoreMatchers.equalTo("CreateMessagesFromEvent"));

		Map<String, Object> step3 = journaledRows.get(3);
		MatcherAssert.assertThat(step3.get("payload_id"), CoreMatchers.equalTo(message1.getHeader().getId()));
		MatcherAssert.assertThat(step3.get("step_name"), CoreMatchers.equalTo("SendEmail"));

		Map<String, Object> step4 = journaledRows.get(4);
		MatcherAssert.assertThat(step4.get("payload_id"), CoreMatchers.equalTo(message2.getHeader().getId()));
		MatcherAssert.assertThat(step4.get("step_name"), CoreMatchers.equalTo("CreateMessagesFromEvent"));

		Map<String, Object> step5 = journaledRows.get(5);
		MatcherAssert.assertThat(step5.get("payload_id"), CoreMatchers.equalTo(message2.getHeader().getId()));
		MatcherAssert.assertThat(step5.get("step_name"), CoreMatchers.equalTo("SendEmail"));

		Map<String, Object> step6 = journaledRows.get(6);
		MatcherAssert.assertThat(step6.get("payload_id"), CoreMatchers.equalTo(message3.getHeader().getId()));
		MatcherAssert.assertThat(step6.get("step_name"), CoreMatchers.equalTo("CreateMessagesFromEvent"));

		Map<String, Object> step7 = journaledRows.get(7);
		MatcherAssert.assertThat(step7.get("payload_id"), CoreMatchers.equalTo(message3.getHeader().getId()));
		MatcherAssert.assertThat(step7.get("step_name"), CoreMatchers.equalTo("SendEmail"));
	}

	public <T> org.springframework.messaging.Message<T> convertBeanToMessagePayload(T object) {
		Map<String, Object> headers = new HashMap<>();
		headers.put("contentType", "application/json");
		MessageHeaders messageHeaders = new MessageHeaders(headers);
		org.springframework.messaging.Message<T> message = (org.springframework.messaging.Message<T>)messageConverter.toMessage(object, messageHeaders);
		return message;
	}
}
