package com.obj.nc.flows.inputEventRouting.config;

import static com.obj.nc.flows.messageProcessing.MessageProcessingFlowConfig.MESSAGE_PROCESSING_FLOW_INPUT_CHANNEL_ID;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.Pollers;
import org.springframework.integration.router.AbstractMessageRouter;
import org.springframework.messaging.MessageChannel;

import com.obj.nc.flows.inputEventRouting.FlowId2InputMessageRouter;
import com.obj.nc.flows.inputEventRouting.SimpleTypeBasedMessageRouter;
import com.obj.nc.flows.inputEventRouting.extensions.GenericEventProcessorExtension;
import com.obj.nc.functions.processors.event2Message.Event2MessageExtensionsConverter;
import com.obj.nc.functions.sources.genericEvents.GenericEventsSupplier;

import lombok.extern.log4j.Log4j2;

@Configuration
@Log4j2
public class InputEventRoutingFlowConfig {
	
	@Value("${nc.flows.input-evet-routing.type:EXTENSION}")
	private String inputEventRoutingType; 
	
	@Autowired private InputEventRoutingProperties routingProps;	
	@Autowired private GenericEventsSupplier genericEventSupplier;
	
	@Autowired private Event2MessageExtensionsConverter eventConvertingExtension;
	@Qualifier(MESSAGE_PROCESSING_FLOW_INPUT_CHANNEL_ID)
	@Autowired private MessageChannel messageProcessingInputChannel;
	
	@Autowired(required = false)
	private List<GenericEventProcessorExtension<?>> eventProcessors = new ArrayList<>();

	
    public static final String GENERIC_EVENT_CHANNEL_ADAPTER_BEAN_NAME = "genericEventSupplierFlowId"; 
    
    //Was done using @ConditionalOnProperty and @ConditionalOnBean but there was a problem with injection order.
    @Bean
    public IntegrationFlow eventRoutingFlow() {
    	if ("FLOW_ID".equals(inputEventRoutingType)) {
    		return flowIdBasedRoutingFlow();
    	} else if ("PAYLOAD_TYPE".equals(inputEventRoutingType)) {
    		return payloadTypeBasedRoutingFlow();
    	} else {
    		if (eventProcessors.size() == 0) {
    			log.warn("nc.flows.input-evet-routing.type property is not set to [FLOW_ID|PAYLOAD_TYPE] and no extension implementing GenericEventProcessorExtension is found as @Bean. Events cannot be routed.");
    		}
    		return extensionBasedRoutingFlow();
    	}
    }
     
    private IntegrationFlow flowIdBasedRoutingFlow() {
    	return IntegrationFlows
			.fromSupplier(genericEventSupplier, 
					conf-> conf.poller(Pollers.fixedRate(routingProps.getPollPeriodInMiliSeconds()))
					.id(GENERIC_EVENT_CHANNEL_ADAPTER_BEAN_NAME))
			.route(flowIdRouter())
			.get();
    }

    @Bean
    public AbstractMessageRouter flowIdRouter() {
        return new FlowId2InputMessageRouter();
    }
    
    private IntegrationFlow payloadTypeBasedRoutingFlow() {
    	return IntegrationFlows
			.fromSupplier(genericEventSupplier, 
					conf-> conf.poller(Pollers.fixedRate(routingProps.getPollPeriodInMiliSeconds()))
					.id(GENERIC_EVENT_CHANNEL_ADAPTER_BEAN_NAME))
			.channel(new DirectChannel())
			.route(simplePayloadTypeBasedRouter())
			.get();
    }

    @Bean
    public AbstractMessageRouter simplePayloadTypeBasedRouter() {
        return new SimpleTypeBasedMessageRouter(); 
    }
    
    private IntegrationFlow extensionBasedRoutingFlow() {
    	return IntegrationFlows
			.fromSupplier(genericEventSupplier, 
					conf-> conf.poller(Pollers.fixedRate(routingProps.getPollPeriodInMiliSeconds()))
					.id(GENERIC_EVENT_CHANNEL_ADAPTER_BEAN_NAME))
			.handle(eventConvertingExtension)
			.split()
			.channel(messageProcessingInputChannel)
			.get();
    }

    
    @Bean
    public GenericEventsSupplier genericEventSupplier() {
    	return new GenericEventsSupplier();
    }

}
