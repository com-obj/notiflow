package com.obj.nc.services;

import com.obj.nc.domain.event.GenericEvent;
import com.obj.nc.repositories.GenericEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EventsServiceImpl implements EventsService {
    
    private final GenericEventRepository genericEventRepository;
    
    @Override
    public Page<GenericEvent> findAllEvents(Instant consumedFrom, Instant consumedTo, Pageable pageable) {
        if (consumedFrom == null) {
            consumedFrom = OffsetDateTime.of(LocalDate.of(2000, 1, 1), LocalTime.NOON, ZoneOffset.UTC).toInstant();
        }
    
        if (consumedTo == null) {
            consumedTo = OffsetDateTime.of(LocalDate.of(9999, 1, 1), LocalTime.NOON, ZoneOffset.UTC).toInstant();
        }
    
        List<GenericEvent> events = genericEventRepository.findAllByTimeConsumedBetween(consumedFrom, consumedTo, pageable);
        long eventsTotalCount = genericEventRepository.countAllByTimeConsumedBetween(consumedFrom, consumedTo);
        return new PageImpl<>(events, pageable, eventsTotalCount);
    }
    
}
