package com.obj.nc.functions.processors.eventIdGenerator;

import static org.hamcrest.MatcherAssert.assertThat;

import org.hamcrest.CoreMatchers;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import com.obj.nc.domain.headers.Header;
import com.obj.nc.domain.message.SimpleTextMessage;

class GenerateEventIdTest {

	@Test
	void test() {
		//GIVEN
		SimpleTextMessage sms = new SimpleTextMessage();

		//WHEN
		GenerateEventIdProcessingFunction function = new GenerateEventIdProcessingFunction();

		SimpleTextMessage outputNotificationIntent = (SimpleTextMessage)function.apply(sms);

		//THEN
		Header header = outputNotificationIntent.getHeader();
		assertThat(header.getEventIds(), Matchers.hasSize(1));
		assertThat(header.getEventIds().get(0), CoreMatchers.notNullValue());
	}

}
