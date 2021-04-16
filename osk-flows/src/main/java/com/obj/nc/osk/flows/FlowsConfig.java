package com.obj.nc.osk.flows;

import static com.obj.nc.flows.intenToMessageToSender.NotificationIntentProcessingFlowConfig.INTENT_PROCESSING_FLOW_INPUT_CHANNEL_ID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.PublishSubscribeChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.messaging.MessageChannel;

import com.obj.nc.osk.functions.processors.eventConverter.EndOutageEventConverter;
import com.obj.nc.osk.functions.processors.eventConverter.StartOutageEventConverter;

@Configuration
public class FlowsConfig {
	
	@Autowired private StartOutageEventConverter startOutageEventConverter;
	@Autowired private EndOutageEventConverter endOutageEventConverter;
	
	public final static String OUTAGE_START_FLOW_ID = "OUTAGE_START";
	public final static String OUTAGE_END_FLOW_ID = "OUTAGE_END";
	public final static String START_OUTAGE_FLOW_INPUT_CHANNEL_ID = OUTAGE_START_FLOW_ID + "_INPUT";
	public final static String END_OUTAGE_FLOW_INPUT_CHANNEL_ID = OUTAGE_END_FLOW_ID + "_INPUT";
	
	@Bean(START_OUTAGE_FLOW_INPUT_CHANNEL_ID)
	public MessageChannel startOutageFlowInputChangel() {
		return new PublishSubscribeChannel();
	}
	
	@Bean(END_OUTAGE_FLOW_INPUT_CHANNEL_ID)
	public MessageChannel endOutageFlowInputChangel() {
		return new PublishSubscribeChannel();
	}
	
	@Bean
	public IntegrationFlow outageStartFlowDefinition() {
		return IntegrationFlows
				.from(startOutageFlowInputChangel())
				.transform(startOutageEventConverter)
				.split()
				.channel(INTENT_PROCESSING_FLOW_INPUT_CHANNEL_ID)
				.get();
	}
	
	@Bean
	public IntegrationFlow outageEndFlowDefinition() {
		return IntegrationFlows
				.from(endOutageFlowInputChangel())
				.transform(endOutageEventConverter)
				.split()
				.channel(INTENT_PROCESSING_FLOW_INPUT_CHANNEL_ID)
				.get();
	}

}
