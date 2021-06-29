package com.obj.nc.functions.sources.intent;

import com.obj.nc.aspects.DocumentProcessingInfo;
import com.obj.nc.domain.message.Message;
import com.obj.nc.domain.message.MessagePersistantState;
import com.obj.nc.domain.notifIntent.NotificationIntent;
import com.obj.nc.exceptions.PayloadValidationException;
import com.obj.nc.functions.sources.SourceSupplierAdapter;
import com.obj.nc.repositories.MessageRepository;
import com.obj.nc.repositories.NotificationIntentRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.util.Optional;

@DocumentProcessingInfo("InputIntentSupplier")
public class NotificationIntentSupplier extends SourceSupplierAdapter<NotificationIntent> {
	
	@Autowired private NotificationIntentRepository repository;

	@Override
	protected Optional<PayloadValidationException> checkPreCondition(NotificationIntent payload) {
		return Optional.empty();
	}

	@Override
	protected NotificationIntent execute() {
		Optional<NotificationIntent> persistedIntentToProcess = repository.findFirstByTimeConsumedIsNullOrderByTimeCreatedAsc();

		if (!persistedIntentToProcess.isPresent()) {
			return null;
		}
		
		NotificationIntent intentToProcess = persistedIntentToProcess.get();
		intentToProcess.setTimeConsumed(Instant.now());
		repository.save(intentToProcess);
		return intentToProcess;
	}

}
