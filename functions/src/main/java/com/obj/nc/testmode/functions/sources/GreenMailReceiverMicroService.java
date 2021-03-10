package com.obj.nc.testmode.functions.sources;

import com.obj.nc.domain.message.Message;
import com.obj.nc.functions.sources.SourceMicroService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.function.Supplier;

@Configuration
@Log4j2
@ConditionalOnProperty(value = "testmode.enabled", havingValue = "true")
public class GreenMailReceiverMicroService extends SourceMicroService<List<Message>, GreenMailReceiverSourceSupplier> {

    @Autowired
    private GreenMailReceiverSourceSupplier supplier;

    @Bean
    public Supplier<Flux<List<Message>>> receiveGreenMailMessages() {
        return super.executeSourceService();
    }

    @Override
    public GreenMailReceiverSourceSupplier getSourceSupplier() {
        return supplier;
    }


}
