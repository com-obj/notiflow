package com.obj.nc.functions.sink.intentPersister;

import com.obj.nc.exceptions.PayloadValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.obj.nc.domain.notifIntent.NotificationIntent;
import com.obj.nc.functions.sink.SinkConsumerAdapter;
import com.obj.nc.repositories.NotificationIntentRepository;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;

import java.util.Optional;

@Component
@AllArgsConstructor
@Log4j2
public class NotificationIntentPersister extends SinkConsumerAdapter<NotificationIntent> {
	
	@Autowired
	private NotificationIntentRepository intentRepository;
	
	protected Optional<PayloadValidationException> checkPreCondition(NotificationIntent payload) {
		if (payload == null) {
			return Optional.of(new PayloadValidationException("Could not persist NotificationIntent because its null. Payload: " + payload));
		}
		return Optional.empty();
	}
	
	protected void execute(NotificationIntent payload) {
		log.debug("Persisting notification intent {}",payload);
		intentRepository.save(payload);
	}

}
