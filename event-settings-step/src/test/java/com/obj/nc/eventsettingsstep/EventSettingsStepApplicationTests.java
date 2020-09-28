package com.obj.nc.eventsettingsstep;

import java.util.concurrent.TimeUnit;

import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.messaging.Processor;
import org.springframework.cloud.stream.test.binder.MessageCollector;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.test.context.junit4.SpringRunner;

import com.obj.nc.domain.event.Event;

@RunWith(SpringRunner.class)
@SpringBootTest
public class EventSettingsStepApplicationTests {

	@Autowired
	private Processor processor;

	@Autowired
	private MessageCollector messageCollector;
	
	public EventSettingsStepApplicationTests() {
		
	}

	@Test
	public void testEmailRecipientsEnrichment() throws Exception {
		Event inputEvent = Event.createWithSimpleMessage("test-config", "Hi there!!");
		
		this.processor.input()
				.send(MessageBuilder.withPayload(inputEvent.toJSONString()).build());
		
		Message<String> message = (Message<String>) messageCollector.forChannel(this.processor.output()).poll(1, TimeUnit.SECONDS);
		
		Event outputEvent = Event.fromJSON(message.getPayload());
		MatcherAssert.assertThat(outputEvent.getHeader().getId(), CoreMatchers.notNullValue());
		MatcherAssert.assertThat(outputEvent.getHeader().getRecipients().size(), CoreMatchers.is(2));
	}
}
