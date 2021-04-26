package com.obj.nc.koderia.functions.sources;

import java.util.function.Supplier;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import com.obj.nc.functions.sources.AsyncSourceMicroService;
import com.obj.nc.koderia.dto.EmitEventDto;

import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Flux;

@Component
@Log4j2
public class KoderiaRestMicroService extends AsyncSourceMicroService<EmitEventDto> {

    @Bean
    public Supplier<Flux<EmitEventDto>> emitKoderiaEvent() {
        return super.executeSourceService();
    }

}
