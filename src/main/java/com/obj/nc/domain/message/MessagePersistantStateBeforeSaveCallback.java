package com.obj.nc.domain.message;

import java.sql.SQLIntegrityConstraintViolationException;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.obj.nc.Get;
import com.obj.nc.domain.endpoints.RecievingEndpoint;
import com.obj.nc.repositories.EndpointsRepository;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.conversion.MutableAggregateChange;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Embedded;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.data.relational.core.mapping.event.BeforeSaveCallback;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.obj.nc.domain.content.MessageContent;
import com.obj.nc.domain.headers.Header;

import javax.validation.constraints.NotNull;

@Component
public class MessagePersistantStateBeforeSaveCallback implements BeforeSaveCallback<MessagePersistantState>{
	
	@Override
	public MessagePersistantState onBeforeSave(MessagePersistantState message,
			MutableAggregateChange<MessagePersistantState> messageChange) {
		
		UUID[] endpointIds = message.getEndpointIds();
		List<RecievingEndpoint> referenceEndpoints = message.findReceivingEndpoints();
		if (referenceEndpoints.size()!= endpointIds.length) {
			throw
				new RuntimeException (
						new SQLIntegrityConstraintViolationException("Message is referencing endpoint via endpointIds atribute which cannot be found in the DB")
				);
		}
		
		return message;
	}
	
}
