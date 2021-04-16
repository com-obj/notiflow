package com.obj.nc.functions.processors.eventIdGenerator;

import static org.hamcrest.MatcherAssert.assertThat;

import org.hamcrest.CoreMatchers;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import com.obj.nc.domain.headers.Header;
import com.obj.nc.domain.notifIntent.NotificationIntent;

class GenerateEventIdTest {

	@Test
	void test() {
		//GIVEN
		NotificationIntent inputNotificationIntent = NotificationIntent.createWithSimpleMessage("test-config", "Hi there!!");

		//WHEN
		GenerateEventIdProcessingFunction function = new GenerateEventIdProcessingFunction();

		NotificationIntent outputNotificationIntent = (NotificationIntent)function.apply(inputNotificationIntent);

		//THEN
		Header header = outputNotificationIntent.getHeader();
		assertThat(header.getEventIds(), Matchers.hasSize(1));
		assertThat(header.getEventIds().get(0), CoreMatchers.notNullValue());
	}

}
