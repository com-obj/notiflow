package com.obj.nc.functions.processors.senders.dtos;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import javax.validation.constraints.NotNull;

import org.springframework.data.annotation.Version;

import com.obj.nc.domain.endpoints.RecievingEndpoint;
import com.obj.nc.domain.headers.HasHeader;
import com.obj.nc.domain.headers.Header;
import com.obj.nc.functions.sink.deliveryInfoPersister.domain.DeliveryInfo.DELIVERY_STATUS;

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
@Builder(toBuilder=true)
public class DeliveryInfoSendResult implements HasHeader {
	 
	@Version
	private Integer version;
	
	@NotNull
	private DELIVERY_STATUS status;
	
	@NotNull
	private Instant processedOn;
	
	@NotNull
//	@NotEmpty
	private UUID[] eventIds;
	
	@NotNull
	private RecievingEndpoint recievingEndpoint;

	private Header header;
	
	public List<UUID> getEventIdsAsList() {
		return Arrays.asList(eventIds);
	}
	
	public Header getHeader() {
		if (header==null) {
			header = new Header();
		}
		return header;
	}
	
}
