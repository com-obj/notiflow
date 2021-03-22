package com.obj.nc.osk.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.Pollers;

import com.obj.nc.functions.processors.eventIdGenerator.ValidateAndGenerateEventIdProcessingFunction;
import com.obj.nc.functions.processors.messageBuilder.MessagesFromEventProcessingFunction;
import com.obj.nc.functions.processors.messageTemplating.EmailTemplateFormatter;
import com.obj.nc.functions.processors.senders.EmailSender;
import com.obj.nc.functions.sink.payloadLogger.PaylaodLoggerSinkConsumer;
import com.obj.nc.functions.sink.processingInfoPersister.ProcessingInfoPersisterSinkConsumer;
import com.obj.nc.functions.sink.processingInfoPersister.eventWithRecipients.ProcessingInfoPersisterForEventWithRecipientsSinkConsumer;
import com.obj.nc.functions.sources.genericEvents.GenericEventsForProcessingSupplier;
import com.obj.nc.osk.functions.NotifEventConverterProcessingFunction;

import lombok.AllArgsConstructor;

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
	
	public static String OUTAGE_START_FLOW_ID = "OUTAGE_START_FLOW_ID";
	
	@Bean
	public IntegrationFlow sendNotificationsOnSIAEventFlow() {
		return IntegrationFlows
				.from(genericEventSupplier, conf-> conf.poller(Pollers.fixedRate(1000)))
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
