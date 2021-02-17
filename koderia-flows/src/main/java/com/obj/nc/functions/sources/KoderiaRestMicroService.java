package com.obj.nc.functions.sources;

import com.obj.nc.dto.*;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
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
