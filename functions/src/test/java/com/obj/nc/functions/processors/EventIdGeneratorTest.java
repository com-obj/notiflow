package com.obj.nc.functions.processors;

import static org.hamcrest.MatcherAssert.assertThat;

import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.Test;

import com.obj.nc.domain.Header;
import com.obj.nc.domain.event.Event;
import com.obj.nc.functions.processors.eventGenerator.ValidateAndGenerateEventIdPreCondition;
import com.obj.nc.functions.processors.eventGenerator.ValidateAndGenerateEventIdExecution;
import com.obj.nc.functions.processors.eventGenerator.ValidateAndGenerateEventIdMicroService;
import com.obj.nc.functions.processors.eventGenerator.ValidateAndGenerateEventIdProcessingFunction;

class EventIdGeneratorTest {

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
		assertThat(header.getEventId(), CoreMatchers.notNullValue());
		//in first step id = eventId
		assertThat(header.getEventId(), CoreMatchers.is(header.getId()));
	}

}
