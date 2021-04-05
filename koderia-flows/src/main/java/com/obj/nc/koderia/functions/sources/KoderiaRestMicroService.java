package com.obj.nc.koderia.functions.sources;

import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import com.obj.nc.functions.sources.AsyncSourceMicroService;
import com.obj.nc.koderia.dto.*;

import reactor.core.publisher.Flux;

import javax.validation.Valid;
import java.util.function.Supplier;

@Component
@Log4j2
public class KoderiaRestMicroService extends AsyncSourceMicroService<EmitEventDto> {

    @Bean
    public Supplier<Flux<EmitEventDto>> emitKoderiaEvent() {
        return super.executeSourceService();
    }

}
