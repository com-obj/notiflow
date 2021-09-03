package com.obj.nc.domain;

import java.util.List;
import java.util.UUID;

public interface HasPreviousEventIds {
	
	void addPreviousEventId(UUID eventId);

	List<UUID> getPreviousEventIds();
	
}
