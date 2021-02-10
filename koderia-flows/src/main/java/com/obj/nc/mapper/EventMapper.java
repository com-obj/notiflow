package com.obj.nc.mapper;

import com.obj.nc.dto.CreateDto;
import com.obj.nc.domain.event.Event;

public interface EventMapper {

    Event map(CreateDto createDto);

}
