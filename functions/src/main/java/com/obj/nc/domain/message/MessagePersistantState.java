package com.obj.nc.domain.message;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.obj.nc.Get;
import com.obj.nc.domain.endpoints.RecievingEndpoint;
import com.obj.nc.repositories.EndpointsRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Embedded;
import org.springframework.data.relational.core.mapping.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.obj.nc.domain.content.MessageContent;
import com.obj.nc.domain.headers.Header;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.SneakyThrows;

@Data
@Table("nc_message")
@Builder
public class MessagePersistantState implements Persistable<UUID>{
	
	@PersistenceConstructor
	// @Value("null") on receivingEndpoints is needed @Transient to work properly when using lombok's @Builder
	public MessagePersistantState(UUID id, Instant timeCreated, Header header, MessageContent body, String messageClass,
								  List<String> endpointIds, @Value("null") List<RecievingEndpoint> receivingEndpoints) {
		this.id = id;
		this.timeCreated = timeCreated;
		this.header = header;
		this.body = body;
		this.messageClass = messageClass;
		this.endpointIds = endpointIds;
		this.receivingEndpoints = receivingEndpoints;
	}
	
	@Id
	@EqualsAndHashCode.Include
	private UUID id;
	@CreatedDate
	private Instant timeCreated;
	@Embedded(onEmpty = Embedded.OnEmpty.USE_NULL)
	private Header header;
	
	@Column("content_json")
	private MessageContent body;
	
	private String messageClass;
	
	@Column("endpoint_ids")
	private List<String> endpointIds;
	
	@JsonIgnore
	@Transient
	private List<RecievingEndpoint> receivingEndpoints;
	
	@Override
	@JsonIgnore
	@Transient
	public boolean isNew() {
		return timeCreated == null;
	}
	
	@SuppressWarnings({"rawtypes", "unchecked"})
	@SneakyThrows
	public <T extends Message> T toMessage() {
		T msg = (T)Class.forName(messageClass).newInstance();
		msg.setBody(getBody());
		msg.setHeader(getHeader());
		msg.setId(getId());
		msg.setTimeCreated(getTimeCreated());
		
		List<RecievingEndpoint> endpoints = findReceivingEndpoints();
		msg.setRecievingEndpoints(endpoints);
		
		return msg;
	}
	
	private List<RecievingEndpoint> findReceivingEndpoints() {
		if (receivingEndpoints == null) {
			receivingEndpoints = Get.getBean(EndpointsRepository.class).findByIds(getEndpointIds().toArray(new String[0]));
		}
		return receivingEndpoints;
	}
	
}
