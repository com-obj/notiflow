package com.obj.nc.osk.repositories;

import java.util.Optional;

import com.obj.nc.domain.event.GenericEvent;

public interface CustomGenericEventRepository {
	
	 Optional<GenericEvent> findStartEventByTicketId(String ticketId);

}