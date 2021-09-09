package com.obj.nc.repositories;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import com.obj.nc.functions.processors.deliveryInfo.domain.DeliveryInfo;
import com.obj.nc.functions.processors.deliveryInfo.domain.DeliveryInfo.DELIVERY_STATUS;

public interface DeliveryInfoRepository extends CrudRepository<DeliveryInfo, UUID> {
	
	List<DeliveryInfo> findByEventIdAndStatusOrderByProcessedOn(UUID eventId, DELIVERY_STATUS status);
	
	List<DeliveryInfo> findByEventIdOrderByProcessedOn(UUID eventId);
	
	List<DeliveryInfo> findByStatus(DELIVERY_STATUS status);
	
	long countByEventIdAndStatus(UUID eventId, DELIVERY_STATUS status);
	
	@Query("select * " +
			"from nc_delivery_info " +
			"where event_id = (:eventId) " +
			"and ((:endpointId)::uuid is null or endpoint_id = (:endpointId)::uuid) " +
			"order by processed_on")
	List<DeliveryInfo> findByEventIdAndEndpointIdOrderByProcessedOn(@Param("eventId") UUID eventId,
																	@Param("endpointId") UUID endpointId);
	
	List<DeliveryInfo> findByEndpointIdOrderByProcessedOn(UUID endpointId);
	
	@Query("select * " +
			"from nc_delivery_info di " +
			"where di.message_id = any (" +
				"with recursive msg_chain as ( " +
					"select msg.id, msg.previous_message_ids " +
					"from nc_message msg " +
					"where msg.id = (:messageId) " +
				"union all " +
					"select next_msg.id, next_msg.previous_message_ids " +
					"from nc_message next_msg " +
					"join msg_chain on msg_chain.id = any ( next_msg.previous_message_ids ))" +
				"select msg_chain.id from msg_chain)" +
			"order by di.processed_on")
	List<DeliveryInfo> findByMessageIdOrderByProcessedOn(@Param("messageId") UUID messageId);
	
	@Query("select * " +
			"from nc_delivery_info di " +
			"where di.status = (:status) " +
			"and di.message_id = any (" +
				"with recursive msg_chain as ( " +
					"select msg.id, msg.previous_message_ids " +
					"from nc_message msg " +
					"where msg.id = (:messageId) " +
				"union all " +
					"select next_msg.id, next_msg.previous_message_ids " +
					"from nc_message next_msg " +
					"join msg_chain on msg_chain.id = any ( next_msg.previous_message_ids ))" +
				"select msg_chain.id from msg_chain)")
	List<DeliveryInfo> findByMessageIdAndStatus(@Param("messageId") UUID messageId,
												@Param("status") DELIVERY_STATUS status);
	
	@Query("select count(di.id) " +
			"from nc_delivery_info di " +
			"where di.status = (:status) " +
			"and di.message_id = any (" +
				"with recursive msg_chain as ( " +
					"select msg.id, msg.previous_message_ids " +
					"from nc_message msg " +
					"where msg.id = (:messageId) " +
				"union all " +
					"select next_msg.id, next_msg.previous_message_ids " +
					"from nc_message next_msg " +
					"join msg_chain on msg_chain.id = any ( next_msg.previous_message_ids ))" +
				"select msg_chain.id from msg_chain)")
	long countByMessageIdAndStatus(@Param("messageId") UUID messageId, 
								   @Param("status") DELIVERY_STATUS status);

}
