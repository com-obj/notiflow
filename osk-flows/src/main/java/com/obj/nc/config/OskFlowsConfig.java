package com.obj.nc.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.PollerSpec;
import org.springframework.integration.dsl.Pollers;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.support.PeriodicTrigger;

import com.obj.nc.functions.processors.eventIdGenerator.ValidateAndGenerateEventIdProcessingFunction;
import com.obj.nc.functions.processors.koderia.RecepientsUsingKoderiaSubscriptionProcessingFunction;
import com.obj.nc.functions.processors.messageBuilder.MessagesFromEventProcessingFunction;
import com.obj.nc.functions.processors.senders.EmailSenderSinkProcessingFunction;
import com.obj.nc.functions.sink.payloadLogger.PaylaodLoggerSinkConsumer;
import com.obj.nc.functions.sink.processingInfoPersister.ProcessingInfoPersisterSinkConsumer;
import com.obj.nc.functions.sink.processingInfoPersister.eventWithRecipients.ProcessingInfoPersisterForEventWithRecipientsMicroService;
import com.obj.nc.functions.sink.processingInfoPersister.eventWithRecipients.ProcessingInfoPersisterForEventWithRecipientsSinkConsumer;
import com.obj.nc.functions.sources.eventGenerator.EventGeneratorSourceSupplier;

@Configuration
public class OskFlowsConfig {

	@Autowired
	private EventGeneratorSourceSupplier eventSupplier;

	@Autowired
	private ValidateAndGenerateEventIdProcessingFunction generateEventId;

	@Autowired
	private RecepientsUsingKoderiaSubscriptionProcessingFunction resolveRecipients;

	@Autowired
	private MessagesFromEventProcessingFunction generateMessagesFromEvent;

	@Autowired
	private EmailSenderSinkProcessingFunction sendMessage;

	@Autowired
	private PaylaodLoggerSinkConsumer logConsumer;
	
	@Autowired
	private ProcessingInfoPersisterSinkConsumer processingInfoPersister;
	
	@Autowired
	private ProcessingInfoPersisterForEventWithRecipientsSinkConsumer processingInfoWithRecipientsPersister;

	@Bean
	public IntegrationFlow sendMessageFlow() {
		return IntegrationFlows.from(
					eventSupplier, 
					configurer -> configurer.poller(sourcePoller()).id("eventSupplier"))
				.transform(generateEventId)
				 	.wireTap(flow -> flow.handle(processingInfoPersister))
				.transform(resolveRecipients)
					.wireTap(flow -> flow.handle(processingInfoPersister))
				.transform(generateMessagesFromEvent)
				.split()
				 	.wireTap(flow -> flow.handle(processingInfoPersister))
				.transform(sendMessage)
					.wireTap(flow -> flow.handle(processingInfoWithRecipientsPersister))
				.handle(logConsumer).get();
	}

	@Bean
	public Trigger sourceTrigger() {
		return new PeriodicTrigger(1000);
	}

	@Bean
	public PollerSpec sourcePoller() {
		return Pollers.trigger(sourceTrigger());
	}
}
