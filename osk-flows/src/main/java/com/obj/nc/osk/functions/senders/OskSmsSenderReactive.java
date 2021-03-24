package com.obj.nc.osk.functions.senders;

import com.obj.nc.domain.message.Message;
import com.obj.nc.functions.processors.ProcessorMicroService;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Flux;

import java.util.function.Function;

@Configuration
@Log4j2
@AllArgsConstructor
public class OskSmsSenderReactive extends ProcessorMicroService<Message, Message, OskSmsSender> {

    private final OskSmsSender fn;

    @Bean
    public Function<Flux<Message>, Flux<Message>> sendSms() {
        return super.executeProccessingService();
    }

    @Override
    public OskSmsSender getProccessingFuction() {
        return fn;
    }

}
