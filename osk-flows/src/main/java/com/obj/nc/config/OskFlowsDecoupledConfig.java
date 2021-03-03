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

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

//@Configuration
public class OskFlowsDecoupledConfig {

//    @Autowired
//    private Supplier<Flux<Event>> generateEvent;
//
//    @Autowired
//    private Function<Flux<Event>, Flux<Message>> generateMessagesFromEvent;
//
//    @Autowired
//    private Function<Flux<Event>, Flux<Event>> resolveRecipients;
//
//    @Autowired
//    private Function<Flux<Message>, Flux<Message>> sendMessage;
//
//    @Autowired
//    private Consumer<Flux<Message>> logEvent;
//
//    @Autowired
//    private Consumer<Flux<BasePayload>> persistPIForEvent;
//
//    @Bean
//    public IntegrationFlow generateEventFlow() {
//        return IntegrationFlows
//                .from(generateEvent, configurer -> configurer.poller(Pollers.fixedDelay(1000)))
//                .channel("generateEventChannel")
//                .get();
//    }
//
//    @Bean
//    public IntegrationFlow resolveRecipientsFlow() {
//        return IntegrationFlows
//                .from("generateEventChannel")
//                .bridge()
//                .transform(resolveRecipients)
//                .channel("resolveRecipientsChannel")
//                .get();
//    }
//
//    @Bean
//    public IntegrationFlow generateMessagesFromEventFlow() {
//        return IntegrationFlows
//                .from("resolveRecipientsChannel")
//                .bridge()
//                .transform(generateMessagesFromEvent)
//                .channel("generateMessagesFromEventChannel")
//                .get();
//    }
//
//    @Bean
//    public IntegrationFlow sendMessageFlow() {
//        return IntegrationFlows
//                .from("generateMessagesFromEventChannel")
//                .bridge()
//                .transform(sendMessage)
//                .channel("sendMessageChannel")
//                .get();
//    }
//
//    @Bean
//    public IntegrationFlow persistPIForEventFlow() {
//        return IntegrationFlows
//                .from("sendMessageChannel")
//                .bridge()
//                .handle(persistPIForEvent)
//                .get();
//    }
//
//    @Bean
//    public IntegrationFlow logEventFlow() {
//        return IntegrationFlows
//                .from("sendMessageChannel")
//                .bridge()
//                .handle(logEvent)
//                .get();
//    }

}
