package com.obj.nc.controller;

import com.obj.nc.dto.*;
import com.obj.nc.functions.sources.KoderiaRestMicroService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Validated
@RestController
@RequestMapping("/events/emit")
@Log4j2
public class EmitEventsRestController {

    @Autowired
    private KoderiaRestMicroService koderiaRestMicroService;

    @PostMapping(path = "/jobPost")
    public void emitJobPostEvent(@Valid @RequestBody EmitJobPostEventDto emitJobPostEventDto) {
        koderiaRestMicroService.onNext(emitJobPostEventDto);
    }

    @PostMapping(path = "/blog")
    public void emitBlogEvent(@Valid @RequestBody EmitBlogEventDto emitBlogEventDto) {
        koderiaRestMicroService.onNext(emitBlogEventDto);
    }

    @PostMapping(path = "/event")
    public void emitEventEvent(@Valid @RequestBody EmitEventEventDto emitEventEventDto) {
        koderiaRestMicroService.onNext(emitEventEventDto);
    }

    @PostMapping(path = "/link")
    public void emitLinkEvent(@Valid @RequestBody EmitLinkEventDto emitLinkEventDto) {
        koderiaRestMicroService.onNext(emitLinkEventDto);
    }

    @PostMapping(path = "/news")
    public void emitNewsEvent(@Valid @RequestBody EmitNewsEventDto emitNewsEventDto) {
        koderiaRestMicroService.onNext(emitNewsEventDto);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    ResponseEntity<String> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        return new ResponseEntity<>("Not valid due to validation error: " + e.getMessage(), HttpStatus.BAD_REQUEST);
    }

}
