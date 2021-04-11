package com.obj.nc.domain.event;

import java.time.Instant;
import java.util.UUID;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;

import lombok.AllArgsConstructor;
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
@Table("nc_event_2_endpoint_delivery")
public class Event2EnpointDelivery implements Persistable<UUID> {
	
	public static enum DELIVERY_STATUS {
		DELIVERED
	}

	@Id
	private UUID id;

	@CreatedDate
	private Instant timeCreated;
	
	private DELIVERY_STATUS[] status;
	private Instant daliveredOn;
	
	private UUID[] eventIds;
	
	private String endpointId;
	
	@Override
	public boolean isNew() {
		return timeCreated == null;
	}
}
