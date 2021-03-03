package com.obj.nc.config;

import com.obj.nc.domain.BasePayload;
import com.obj.nc.domain.event.Event;
import com.obj.nc.domain.message.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.Pollers;
import reactor.core.publisher.Flux;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

@Configuration
public class OskFlowsConfig {

    @Autowired
    private Supplier<Flux<Event>> generateEvent;

    @Autowired
    private Function<Flux<Event>, Flux<Event>> resolveRecipients;

    @Autowired
    private Function<Flux<Event>, Flux<Message>> generateMessagesFromEvent;

    @Autowired
    private Function<Flux<Message>, Flux<Message>> sendMessage;

    @Autowired
    private Consumer<Flux<BasePayload>> persistPIForEvent;

    @Autowired
    private Consumer<Flux<Message>> logEvent;

    @Bean
    public IntegrationFlow sendMessageFlow() {
        return IntegrationFlows
                .from(generateEvent, configurer -> configurer.poller(Pollers.fixedDelay(1000)))
                .transform(resolveRecipients)
                .transform(generateMessagesFromEvent)
                .transform(sendMessage)
//                .publishSubscribeChannel(pubSub -> pubSub
//                        .subscribe(flow -> flow.handle(persistPIForEvent))
//                        .subscribe(flow -> flow.handle(logEvent)))
                .handle(logEvent)
                .get();
    }

}
