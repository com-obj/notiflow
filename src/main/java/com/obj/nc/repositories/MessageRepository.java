package com.obj.nc.repositories;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Pageable;

import com.obj.nc.domain.message.MessagePersistentState;
import com.obj.nc.domain.refIntegrity.EntityExistenceChecker;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface MessageRepository extends PagingAndSortingRepository<MessagePersistentState, UUID>, EntityExistenceChecker<UUID> {
	
	List<MessagePersistentState> findByIdIn(List<UUID> intentIds);
    
    List<MessagePersistentState> findAllByTimeCreatedBetween(Instant createdFrom, Instant createdTo, Pageable pageable);
    
    long countAllByTimeCreatedBetween(Instant createdFrom, Instant createdTo);
    
}
