package com.obj.nc.functions.sink.deliveryInfoPersister.domain;

import java.time.Instant;
import java.util.UUID;

import javax.validation.constraints.NotNull;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;

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
		PROCESSING, SENT, DELIVERED, FAILED
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
	
	@NotNull
	private UUID eventId;
	
	@NotNull
	private String endpointId;
	
	private UUID failedPayloadId;

	@Override
	public boolean isNew() {
		return processedOn == null;
	}
	
}
