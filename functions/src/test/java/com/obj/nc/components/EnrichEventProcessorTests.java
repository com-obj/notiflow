package com.obj.nc.components;

import java.util.function.Function;

import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import com.obj.nc.domain.event.Event;
import com.obj.nc.functions.processors.koderia.FindRecepientsUsingKoderiaSubsription;

@RunWith(SpringRunner.class)
@JdbcTest
@ContextConfiguration(classes = FindRecepientsUsingKoderiaSubsription.class)
class EnrichEventProcessorTests {

	@Autowired
	Function<Event, Event> enrichEvent;

	@Test
	public void contextLoads() {
//		Event inputEvent = Event.createWithSimpleMessage("test-config", "Hi there!!");
//
//		Event outputEvent = enrichEvent.apply(inputEvent);
//
//		MatcherAssert.assertThat(outputEvent.getHeader().getId(), CoreMatchers.notNullValue());
//		MatcherAssert.assertThat(outputEvent.getHeader().getRecipients().size(), CoreMatchers.is(2));
	}

}
