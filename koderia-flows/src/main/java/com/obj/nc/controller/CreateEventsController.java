package com.obj.nc.controller;

import com.obj.nc.domain.event.Event;
import com.obj.nc.dto.*;
import com.obj.nc.services.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.ConstraintViolationException;
import javax.validation.Valid;

@Validated
@RestController
@RequestMapping("/events/create")
public class CreateEventsController {

    @Autowired
    private EventService eventService;

    @PostMapping(path = "/jobPost")
    @ResponseStatus(HttpStatus.CREATED)
    public Event createJobPost(@Valid @RequestBody CreateJobPostDto createJobPostDto) {
        return eventService.createEvent(createJobPostDto);
    }

    @PostMapping(path = "/blog")
    @ResponseStatus(HttpStatus.CREATED)
    public Event createBlog(@Valid @RequestBody CreateBlogDto createBlogDto) {
        return eventService.createEvent(createBlogDto);
    }

    @PostMapping(path = "/event")
    @ResponseStatus(HttpStatus.CREATED)
    public Event createEvent(@Valid @RequestBody CreateEventDto createEventDto) {
        return eventService.createEvent(createEventDto);
    }

    @PostMapping(path = "/link")
    @ResponseStatus(HttpStatus.CREATED)
    public Event createLink(@Valid @RequestBody CreateLinkDto createLinkDto) {
        return eventService.createEvent(createLinkDto);
    }

    @PostMapping(path = "/news")
    @ResponseStatus(HttpStatus.CREATED)
    public Event createNews(@Valid @RequestBody CreateNewsDto createNewsDto) {
        return eventService.createEvent(createNewsDto);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    ResponseEntity<String> handleConstraintViolationException(ConstraintViolationException e) {
        return new ResponseEntity<>("Not valid due to validation error: " + e.getMessage(), HttpStatus.BAD_REQUEST);
    }

}
