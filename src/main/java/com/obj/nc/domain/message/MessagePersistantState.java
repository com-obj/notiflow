package com.obj.nc.domain.message;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import javax.validation.constraints.NotNull;

import com.obj.nc.domain.HasMessageIds;
import com.obj.nc.repositories.MessageRepository;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Embedded;
import org.springframework.data.relational.core.mapping.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.obj.nc.Get;
import com.obj.nc.domain.HasEventIds;
import com.obj.nc.domain.HasRecievingEndpoints;
import com.obj.nc.domain.content.MessageContent;
import com.obj.nc.domain.endpoints.RecievingEndpoint;
import com.obj.nc.domain.headers.HasHeader;
import com.obj.nc.domain.headers.Header;
import com.obj.nc.domain.refIntegrity.Reference;
import com.obj.nc.repositories.EndpointsRepository;
import com.obj.nc.repositories.GenericEventRepository;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.SneakyThrows;

@Data
@Table("nc_message")
public class MessagePersistantState implements Persistable<UUID>, HasEventIds, HasMessageIds, HasHeader, HasRecievingEndpoints {
	

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
	
	@NotNull
	@Reference(EndpointsRepository.class)
	private UUID[] endpointIds;
	
	@JsonIgnore
	@Transient
	private List<RecievingEndpoint> receivingEndpoints;
	
	@Override
	@JsonIgnore
	@Transient
	public boolean isNew() {
		return timeCreated == null;
	}
	
	@Override
	@JsonIgnore
	@Transient
	@Reference(GenericEventRepository.class)
	public List<UUID> getEventIds() {
		return getHeader().getEventIds();
	}
	
	@Override
	@JsonIgnore
	@Transient
	public List<UUID> getMessageIds() {
		return getHeader().getMessageIds();
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
	
	public List<RecievingEndpoint> findReceivingEndpoints() {
		if (receivingEndpoints == null) {
			receivingEndpoints = Get.getEndpointsRepo().findByIds(getEndpointIds());
		}
		return receivingEndpoints;
	}

	@Override
	public List<? extends RecievingEndpoint> getRecievingEndpoints() {
		return findReceivingEndpoints();
	}
	
}
