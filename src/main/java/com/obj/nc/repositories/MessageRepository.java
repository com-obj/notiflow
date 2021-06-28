package com.obj.nc.repositories;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.repository.CrudRepository;

import com.obj.nc.domain.message.EmailMessage;
import com.obj.nc.domain.message.Message;
import com.obj.nc.domain.message.MessagePersistantState;

public interface MessageRepository extends CrudRepository<MessagePersistantState, UUID> {

	
	List<MessagePersistantState> findByIdIn(List<UUID> intentIds);
	
	Optional<MessagePersistantState> findFirstByTimeConsumedIsNullOrderByTimeCreatedAsc();
}
