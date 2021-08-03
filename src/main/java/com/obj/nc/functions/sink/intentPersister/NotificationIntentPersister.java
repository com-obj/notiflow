package com.obj.nc.functions.sink.intentPersister;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.obj.nc.domain.notifIntent.NotificationIntent;
import com.obj.nc.functions.sink.SinkConsumerAdapter;
import com.obj.nc.repositories.NotificationIntentRepository;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Component
@AllArgsConstructor
@Log4j2
public class NotificationIntentPersister extends SinkConsumerAdapter<NotificationIntent> {

    @Autowired
    private NotificationIntentRepository intentRepo;

    @Override
	protected void execute(NotificationIntent notifIntent) {
		intentRepo.save(notifIntent);
	}



}
