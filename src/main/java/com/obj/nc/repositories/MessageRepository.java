package com.obj.nc.repositories;

import java.util.List;
import java.util.UUID;

import org.springframework.data.repository.CrudRepository;

import com.obj.nc.domain.message.MessagePersistentState;
import com.obj.nc.domain.refIntegrity.EntityExistenceChecker;

public interface MessageRepository extends CrudRepository<MessagePersistentState, UUID>, EntityExistenceChecker<UUID> {

	
	List<MessagePersistentState> findByIdIn(List<UUID> intentIds);
}
