package com.obj.nc.osk.config;

import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.http.HttpHeaders;
import org.springframework.integration.http.dsl.Http;

import com.obj.nc.functions.processors.dummy.DummyRecepientsEnrichmentProcessingFunction;
import com.obj.nc.functions.processors.eventIdGenerator.ValidateAndGenerateEventIdProcessingFunction;
import com.obj.nc.functions.processors.messageBuilder.MessagesFromEventProcessingFunction;
import com.obj.nc.functions.processors.senders.EmailSenderSinkProcessingFunction;
import com.obj.nc.functions.sink.payloadLogger.PaylaodLoggerSinkConsumer;
import com.obj.nc.functions.sink.processingInfoPersister.ProcessingInfoPersisterSinkConsumer;
import com.obj.nc.functions.sink.processingInfoPersister.eventWithRecipients.ProcessingInfoPersisterForEventWithRecipientsSinkConsumer;
import com.obj.nc.osk.functions.NotificationEventConverterProcessingFunction;
import com.obj.nc.osk.sia.dto.IncidentTicketNotificationEventDto;

@Configuration
public class FlowsConfig {
	
	@Autowired
	private NotificationEventConverterProcessingFunction  siaNotifEventConverter;

	@Autowired
	private ValidateAndGenerateEventIdProcessingFunction generateEventId;

	@Autowired
	private DummyRecepientsEnrichmentProcessingFunction resolveRecipients;

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
	public IntegrationFlow sendNotificationsOnSIAEventFlow() {
		return IntegrationFlows
				.from(
						Http
						.inboundGateway(SIAInboundGatewayConfig.NOTIFICATION_EVENT_REST_ENDPOINT_URL)
						.requestMapping(m -> m.methods(HttpMethod.POST))
						.requestPayloadType(IncidentTicketNotificationEventDto.class)
				)
				.publishSubscribeChannel(
					publishSubscribeSpec -> publishSubscribeSpec
					.id(SIAInboundGatewayConfig.NOTIFICATION_EVENT_INPUT_CHANNEL_NAME)
					.subscribe(
						flow -> flow
						.transform((payload) -> "OK")
						.enrichHeaders(Collections.singletonMap(HttpHeaders.STATUS_CODE, HttpStatus.ACCEPTED))
						)
					)
				.transform(siaNotifEventConverter)
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

}
