package com.obj.nc.repositories;

import java.util.UUID;

import org.springframework.data.repository.CrudRepository;

import com.obj.nc.domain.message.Message;

public interface MessageRepository extends CrudRepository<Message<?>, UUID> {

}
