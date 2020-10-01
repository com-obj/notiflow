package com.obj.nc.eventpersiststep;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.test.context.junit4.SpringRunner;

import com.obj.nc.domain.event.EmailRecipient;
import com.obj.nc.domain.event.Event;
import com.obj.nc.domain.event.DeliveryOptions.AGGREGATION_TYPE;

import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//@DataJdbcTest
public class EventPersistStepApplicationTests {

	@Autowired
	protected Sink sink;

	@Autowired
	protected PersistEventSink persistEventSink;

	@Test
	public void testEventPersist() throws Exception {
		
		ArgumentCaptor<Event> captor = ArgumentCaptor.forClass(Event.class);
		
		Event event = Event.createWithSimpleMessage("test-config", "Hi there!!");
		EmailRecipient recipient1 = EmailRecipient.create("John Doe", "john.doe@objectify.sk");
		
		EmailRecipient recipient2 = EmailRecipient.create("John Dudly", "john.dudly@objectify.sk");
		recipient2.getDeliveryOptions().setAggregationType(AGGREGATION_TYPE.ONCE_A_WEEK);
		
		event.getHeader()
			.addRecipient(recipient1)
			.addRecipient(recipient2);
		Message<String> message = MessageBuilder.withPayload(event.toJSONString()).build();

		sink
			.input()
			.send(message);
		
		verify(this.persistEventSink).logEvent().accept(captor.capture());
	}

	@EnableAutoConfiguration
	@EnableBinding(Sink.class)
	static class TestConfig {

		// Override `UsageCostLogger` bean for spying.
		@Bean
		@Primary
		public PersistEventSink persistEventSink() {
			return spy(new PersistEventSink());
		}
	}
}