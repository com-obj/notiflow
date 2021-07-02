package com.obj.nc.functions.processors.eventIdGenerator;

import static org.hamcrest.MatcherAssert.assertThat;

import org.hamcrest.CoreMatchers;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import com.obj.nc.domain.headers.Header;
import com.obj.nc.domain.message.SmsMessage;

class GenerateEventIdTest {

	@Test
	void test() {
		//GIVEN
		SmsMessage sms = new SmsMessage();

		//WHEN
		GenerateEventIdProcessingFunction function = new GenerateEventIdProcessingFunction();

		SmsMessage outputNotificationIntent = (SmsMessage)function.apply(sms);

		//THEN
		Header header = outputNotificationIntent.getHeader();
		assertThat(header.getEventIds(), Matchers.hasSize(1));
		assertThat(header.getEventIds().get(0), CoreMatchers.notNullValue());
	}

}
