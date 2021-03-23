package com.obj.nc.osk.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.channel.PublishSubscribeChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.Pollers;
import org.springframework.messaging.MessageChannel;

import com.obj.nc.functions.processors.eventIdGenerator.ValidateAndGenerateEventIdProcessingFunction;
import com.obj.nc.functions.processors.messageBuilder.MessagesFromEventProcessingFunction;
import com.obj.nc.functions.processors.messageTemplating.EmailTemplateFormatter;
import com.obj.nc.functions.processors.senders.EmailSender;
import com.obj.nc.functions.sink.payloadLogger.PaylaodLoggerSinkConsumer;
import com.obj.nc.functions.sink.processingInfoPersister.ProcessingInfoPersisterSinkConsumer;
import com.obj.nc.functions.sink.processingInfoPersister.eventWithRecipients.ProcessingInfoPersisterForEventWithRecipientsSinkConsumer;
import com.obj.nc.functions.sources.genericEvents.GenericEventsForProcessingSupplier;
import com.obj.nc.osk.functions.NotifEventConverterProcessingFunction;

@Configuration
public class FlowsConfig {
	
	@Autowired private NotifEventConverterProcessingFunction siaNotifEventConverter;
	@Autowired private ValidateAndGenerateEventIdProcessingFunction generateEventId;
	@Autowired private MessagesFromEventProcessingFunction generateMessagesFromEvent;
	@Autowired private EmailSender sendMessage;
	@Autowired private PaylaodLoggerSinkConsumer logConsumer;
	@Autowired private ProcessingInfoPersisterSinkConsumer processingInfoPersister;
	@Autowired private ProcessingInfoPersisterForEventWithRecipientsSinkConsumer processingInfoWithRecipientsPersister;
	@Autowired private GenericEventsForProcessingSupplier genericEventSupplier;
	@Autowired private EmailTemplateFormatter emailFormatter;
	
	public final static String OUTAGE_START_FLOW_ID = "OUTAGE_START";
	public final static String OUTAGE_END_FLOW_ID = "OUTAGE_END";
	public final static String START_OUTAGE_FLOW_INPUT_CHANNEL_ID = OUTAGE_START_FLOW_ID + "_INPUT";
	public final static String END_OUTAGE_FLOW_INPUT_CHANNEL_ID = OUTAGE_END_FLOW_ID + "_INPUT";
	
//	@Bean
//	public IntegrationFlow sendNotificationsOnSIAEventFlow() {
//		return IntegrationFlows
//				.from(genericEventSupplier, conf-> conf.poller(Pollers.fixedRate(1000)))
//				.transform(siaNotifEventConverter)
//				.split()
//				.transform(generateEventId)
//				 	.wireTap(flow -> flow.handle(processingInfoPersister))
//				.transform(generateMessagesFromEvent)
//				.split()
//				 	.wireTap(flow -> flow.handle(processingInfoPersister))
//				.transform(emailFormatter)
//				.split()
//				.transform(sendMessage)
//				.handle(logConsumer).get();
//	}
	
	@Bean(START_OUTAGE_FLOW_INPUT_CHANNEL_ID)
	public MessageChannel startOutageFlowInputChangel() {
		return new PublishSubscribeChannel();
	}
	
	@Bean(END_OUTAGE_FLOW_INPUT_CHANNEL_ID)
	public MessageChannel endOutageFlowInputChangel() {
		return new PublishSubscribeChannel();
	}
	
	@Bean
	public IntegrationFlow processSIAStartOutageEvent() {
		return IntegrationFlows
				.from(startOutageFlowInputChangel())
				.transform(siaNotifEventConverter)
				.split()
				.transform(generateEventId)
				 	.wireTap(flow -> flow.handle(processingInfoPersister))
				.transform(generateMessagesFromEvent)
				.split()
				 	.wireTap(flow -> flow.handle(processingInfoPersister))
				.transform(emailFormatter)
				.split()
				.transform(sendMessage)
				.handle(logConsumer).get();
	}

}
