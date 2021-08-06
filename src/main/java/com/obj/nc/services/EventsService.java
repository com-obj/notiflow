package com.obj.nc.services;

import com.obj.nc.domain.event.GenericEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;

public interface EventsService {
    
    Page<GenericEvent> findAllEvents(Instant consumedFrom, Instant consumedTo, Pageable pageable);
    
}
