package com.obj.nc.functions.processors.deliveryInfo.domain;

import java.time.Instant;
import java.util.UUID;

import javax.validation.constraints.NotNull;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;

import com.obj.nc.domain.refIntegrity.Reference;
import com.obj.nc.repositories.EndpointsRepository;
import com.obj.nc.repositories.FailedPayloadRepository;
import com.obj.nc.repositories.GenericEventRepository;
import com.obj.nc.repositories.MessageRepository;
import com.obj.nc.repositories.NotificationIntentRepository;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@EqualsAndHashCode(of = "id")
@AllArgsConstructor
@NoArgsConstructor
@Table("nc_delivery_info")
@Builder(toBuilder=true)
public class DeliveryInfo implements Persistable<UUID> {
	
	//Order of elements in this enum is important. Final state should be last
	public static enum DELIVERY_STATUS {
		PROCESSING, SENT, DELIVERED, READ, FAILED
	}

	@Id
	private UUID id;
	
	@Version
	private Integer version;
	
	@NotNull
	private DELIVERY_STATUS status;
	
	@CreatedDate
	@Getter
	private Instant processedOn;
	
	@Reference(GenericEventRepository.class)
	private UUID eventId;
	
	@Reference(NotificationIntentRepository.class)
	private UUID intentId;
	
	@Reference(MessageRepository.class)
	private UUID messageId;
	
	@NotNull
	@Reference(EndpointsRepository.class)
	private UUID endpointId;
	
	@Reference(FailedPayloadRepository.class)
	private UUID failedPayloadId;

	@Override
	public boolean isNew() {
		return processedOn == null;
	}
	
}
