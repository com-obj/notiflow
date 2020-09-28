package com.obj.nc.eventgeneratortest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import com.obj.nc.domain.event.Event;

@EnableScheduling
@EnableBinding(Source.class)
public class EventGenerator {

	@Autowired
	private Source source;

//	private List<Event> events = new ArrayList<Event>();

	@Scheduled(fixedDelay = 5000)
	public void sendEvents() {
		Event event = Event.createWithSimpleMessage("test-config", "Hi there!!");

		this.source.output().send(MessageBuilder.withPayload(event).build());
	}

}
