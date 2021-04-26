package com.obj.nc.koderia.controller;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.obj.nc.koderia.dto.EmitEventDto;
import com.obj.nc.koderia.functions.sources.KoderiaRestMicroService;

import lombok.extern.log4j.Log4j2;

@Validated
@RestController
@RequestMapping("/events")
@Log4j2
public class EmitEventsRestController {

    @Autowired
    private KoderiaRestMicroService koderiaRestMicroService;

    @PostMapping(path = "/emit")
    public void emitEvent(@Valid @RequestBody EmitEventDto emitJobPostEventDto) {
        koderiaRestMicroService.onNext(emitJobPostEventDto);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    ResponseEntity<String> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        return new ResponseEntity<>("Not valid due to validation error: " + e.getMessage(), HttpStatus.BAD_REQUEST);
    }

}
