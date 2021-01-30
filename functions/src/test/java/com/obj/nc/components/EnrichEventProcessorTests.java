package com.obj.nc.components;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import com.obj.nc.functions.processors.koderia.RecepientsUsingKoderiaSubsriptionFinder;
import com.obj.nc.functions.processors.koderia.RecepientsUsingKoderiaSubsriptionFinder.ResolveRecipients;

@RunWith(SpringRunner.class)
@JdbcTest
@ContextConfiguration(classes = RecepientsUsingKoderiaSubsriptionFinder.class)
class EnrichEventProcessorTests {

	@Autowired
	ResolveRecipients enrichEvent;

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
