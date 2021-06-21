package com.obj.nc.repositories;

import com.obj.nc.domain.message.Message;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.UUID;

public interface MessageRepository extends CrudRepository<Message, UUID> {

	List<Message> findByIdIn(List<UUID> intentIds);

}
