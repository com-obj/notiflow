package com.obj.nc.eventgeneratortest.eventgeneratortest;
import java.util.concurrent.TimeUnit;

import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.cloud.stream.test.binder.MessageCollector;
import org.springframework.messaging.Message;
import org.springframework.test.context.junit4.SpringRunner;

import com.obj.nc.domain.event.Event;


@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class EventGeneratorTestApplicationTests {

	@Autowired
	private MessageCollector messageCollector;

	@Autowired
	private Source source;
	
	public EventGeneratorTestApplicationTests() {
		
	}

	@Test
	public void testUsageDetailSender() throws Exception {
		Message<String> message = (Message<String>) messageCollector.forChannel(this.source.output()).poll(1, TimeUnit.SECONDS);
		
		Event event = Event.fromJSON(message.getPayload());
		
		MatcherAssert.assertThat(event.getHeader().getId(), CoreMatchers.notNullValue());
	}

}
