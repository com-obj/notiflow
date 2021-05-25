package com.obj.nc.repositories;

import java.util.List;
import java.util.UUID;

import org.springframework.data.repository.CrudRepository;

import com.obj.nc.domain.message.EmailMessage;

public interface EmailMessageRepository extends CrudRepository<EmailMessage, UUID> {

	List<EmailMessage> findByIdIn(List<UUID> intentIds);

}
