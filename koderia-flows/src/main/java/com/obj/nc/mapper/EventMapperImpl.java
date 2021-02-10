package com.obj.nc.mapper;

import com.obj.nc.dto.CreateDto;
import com.obj.nc.domain.event.Event;
import org.springframework.stereotype.Component;

@Component
public class EventMapperImpl implements EventMapper {

    @Override
    public Event map(CreateDto createDto) {
        Event event = new Event();
        event.getHeader().setConfigurationName("koderia-pipeline");

        event.getBody().getMessage().getContent().setSubject(createDto.getSubject());
        event.getBody().getMessage().getContent().setText(createDto.getText());

        event.getBody().setAttributes(createDto.asMap());
        return event;
    }

}
