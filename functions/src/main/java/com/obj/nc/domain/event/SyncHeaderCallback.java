package com.obj.nc.domain.event;

import org.springframework.core.annotation.Order;
import org.springframework.data.relational.core.mapping.event.AfterLoadCallback;
import org.springframework.stereotype.Component;

@Component
@Order(1)
public class SyncHeaderCallback implements AfterLoadCallback<GenericEvent> {
	
	@Override
	public GenericEvent onAfterLoad(GenericEvent event) {
		event.syncHeaderFields();
		return event;
	}
}
