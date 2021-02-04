package com.obj.nc.functions.processors.eventIdGenerator;

import static org.hamcrest.MatcherAssert.assertThat;

import org.hamcrest.CoreMatchers;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import com.obj.nc.domain.Header;
import com.obj.nc.domain.event.Event;

class ValidateAndGenerateEventIdTest {

	@Test
	void test() {
		//GIVEN
		Event inputEvent = Event.createWithSimpleMessage("test-config", "Hi there!!");

		//WHEN
		ValidateAndGenerateEventIdProcessingFunction function = new ValidateAndGenerateEventIdProcessingFunction(
				new ValidateAndGenerateEventIdExecution(),
				new ValidateAndGenerateEventIdPreCondition());

		Event outputEvent = function.apply(inputEvent);

		//THEN
		Header header = outputEvent.getHeader();
		assertThat(header.getId(), CoreMatchers.notNullValue());
		assertThat(header.getEventIds(), Matchers.hasSize(1));
		assertThat(header.getEventIds().get(0), CoreMatchers.notNullValue());
		//in first step id = eventId
		assertThat(header.getEventIds().get(0), CoreMatchers.is(header.getId()));
	}

}
