package com.obj.nc.services;

import com.obj.nc.domain.event.Event;
import com.obj.nc.mapper.EventMapper;
import com.obj.nc.dto.*;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Log4j2
public class EventServiceImpl implements EventService {

    @Autowired
    private EventMapper mapper;

    @Override
    public Event createEvent(CreateDto createJobPostDto) {
        return mapper.map(createJobPostDto);
    }

}
