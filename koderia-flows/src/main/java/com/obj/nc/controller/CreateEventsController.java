package com.obj.nc.controller;

import com.obj.nc.domain.event.Event;
import com.obj.nc.dto.*;
import com.obj.nc.services.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/events/create")
public class CreateEventsController {

    @Autowired
    private EventService eventService;

    @PostMapping(path = "/jobPost", consumes = "application/json;charset=utf8")
    @ResponseStatus(HttpStatus.CREATED)
    public Event createJobPost(@RequestBody CreateJobPostDto createJobPostDto) {
        return eventService.createEvent(createJobPostDto);
    }

    @PostMapping(path = "/blog")
    @ResponseStatus(HttpStatus.CREATED)
    public Event createBlog(@RequestBody CreateBlogDto createBlogDto) {
        return eventService.createEvent(createBlogDto);
    }

    @PostMapping(path = "/event")
    @ResponseStatus(HttpStatus.CREATED)
    public Event createEvent(@RequestBody CreateEventDto createEventDto) {
        return eventService.createEvent(createEventDto);
    }

    @PostMapping(path = "/link")
    @ResponseStatus(HttpStatus.CREATED)
    public Event createLink(@RequestBody CreateLinkDto createLinkDto) {
        return eventService.createEvent(createLinkDto);
    }

    @PostMapping(path = "/news")
    @ResponseStatus(HttpStatus.CREATED)
    public Event createNews(@RequestBody CreateNewsDto createNewsDto) {
        return eventService.createEvent(createNewsDto);
    }

}
