package com.obj.nc.repositories;

import java.util.List;
import java.util.UUID;

import org.springframework.data.repository.CrudRepository;

import com.obj.nc.domain.message.EmailMessage;
import com.obj.nc.domain.message.Message;
import com.obj.nc.domain.message.MessagePersistantState;
import com.obj.nc.domain.refIntegrity.EntityExistanceChecker;

public interface MessageRepository extends CrudRepository<MessagePersistantState, UUID>, EntityExistanceChecker<UUID> {

	
	List<MessagePersistantState> findByIdIn(List<UUID> intentIds);
}
