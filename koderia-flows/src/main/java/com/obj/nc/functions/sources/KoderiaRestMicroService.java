package com.obj.nc.functions.sources;

import com.obj.nc.dto.*;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import javax.validation.Valid;
import java.util.function.Supplier;

@Validated
@RestController
@RequestMapping("/events/emit")
@Log4j2
public class KoderiaRestMicroService extends AsyncSourceMicroService<EmitEventDto> {

    @Bean
    public Supplier<Flux<EmitEventDto>> emitKoderiaEvent() {
        return super.executeSourceService();
    }

    @PostMapping(path = "/jobPost")
    public void emitJobPostEvent(@Valid @RequestBody EmitJobPostEventDto emitJobPostEventDto) {
        streamSource.onNext(emitJobPostEventDto);
    }

    @PostMapping(path = "/blog")
    public void emitBlogEvent(@Valid @RequestBody EmitBlogEventDto emitBlogEventDto) {
        streamSource.onNext(emitBlogEventDto);
    }

    @PostMapping(path = "/event")
    public void emitEventEvent(@Valid @RequestBody EmitEventEventDto emitEventEventDto) {
        streamSource.onNext(emitEventEventDto);
    }

    @PostMapping(path = "/link")
    public void emitLinkEvent(@Valid @RequestBody EmitLinkEventDto emitLinkEventDto) {
        streamSource.onNext(emitLinkEventDto);
    }

    @PostMapping(path = "/news")
    public void emitNewsEvent(@Valid @RequestBody EmitNewsEventDto emitNewsEventDto) {
        streamSource.onNext(emitNewsEventDto);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    ResponseEntity<String> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        return new ResponseEntity<>("Not valid due to validation error: " + e.getMessage(), HttpStatus.BAD_REQUEST);
    }

}
