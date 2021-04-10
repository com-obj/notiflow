package com.obj.nc.functions.processors.eventIdGenerator;

import static org.hamcrest.MatcherAssert.assertThat;

import org.hamcrest.CoreMatchers;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import com.obj.nc.domain.Header;
import com.obj.nc.domain.notifIntent.NotificationIntent;

class ValidateAndGenerateEventIdTest {

	@Test
	void test() {
		//GIVEN
		NotificationIntent inputNotificationIntent = NotificationIntent.createWithSimpleMessage("test-config", "Hi there!!");

		//WHEN
		ValidateAndGenerateEventIdProcessingFunction function = new ValidateAndGenerateEventIdProcessingFunction();

		NotificationIntent outputNotificationIntent = function.apply(inputNotificationIntent);

		//THEN
		Header header = outputNotificationIntent.getHeader();
		assertThat(header.getId(), CoreMatchers.notNullValue());
		assertThat(header.getEventIds(), Matchers.hasSize(1));
		assertThat(header.getEventIds().get(0), CoreMatchers.notNullValue());
		//in first step id = eventId
		assertThat(header.getEventIds().get(0), CoreMatchers.is(header.getId()));
	}

}
