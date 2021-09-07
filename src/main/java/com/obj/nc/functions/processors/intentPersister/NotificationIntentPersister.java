package com.obj.nc.functions.processors.intentPersister;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.obj.nc.domain.notifIntent.NotificationIntent;
import com.obj.nc.functions.processors.ProcessorFunctionAdapter;
import com.obj.nc.repositories.NotificationIntentRepository;

import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
public class NotificationIntentPersister extends ProcessorFunctionAdapter<NotificationIntent, NotificationIntent> {

    @Autowired
    private NotificationIntentRepository intentRepo;

    @Override
	protected NotificationIntent execute(NotificationIntent notifIntent) {
		return intentRepo.save(notifIntent);
	}



}
