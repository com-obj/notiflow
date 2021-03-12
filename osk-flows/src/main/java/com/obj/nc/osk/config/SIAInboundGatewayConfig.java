package com.obj.nc.osk.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.integration.http.inbound.HttpRequestHandlingMessagingGateway;
import org.springframework.integration.http.inbound.RequestMapping;

import com.obj.nc.osk.sia.dto.IncidentTicketNotificationEventDto;

@Configuration
public class SIAInboundGatewayConfig {
	
	public static final String NOTIFICATION_EVENT_INPUT_CHANNEL_NAME = "notification-event-input-channel";
	public static final String NOTIFICATION_EVENT_REST_ENDPOINT_URL = "/notification-event";

	@Bean
	public HttpRequestHandlingMessagingGateway siaNotificationInboundGateway() {
	    HttpRequestHandlingMessagingGateway gateway =
	        new HttpRequestHandlingMessagingGateway(true);
	    gateway.setRequestMapping(siaNotificationInboundGatewayMapping());
	    gateway.setRequestPayloadTypeClass(IncidentTicketNotificationEventDto.class);
//	    gateway.setRequestChannelName(NOTIFICATION_EVENT_INPUT_CHANNEL_NAME);
	    return gateway;
	}

	@Bean
	public RequestMapping siaNotificationInboundGatewayMapping() {
	    RequestMapping requestMapping = new RequestMapping();
	    requestMapping.setPathPatterns(NOTIFICATION_EVENT_REST_ENDPOINT_URL);
	    requestMapping.setMethods(HttpMethod.POST);
	    return requestMapping;
	}
}
