package com.obj.nc.services;

import com.obj.nc.domain.event.Event;
import com.obj.nc.dto.*;

public interface EventService {

    Event createEvent(CreateDto createDto);

}
