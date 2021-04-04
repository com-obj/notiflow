package com.obj.nc.osk;

import static com.obj.nc.utils.JsonUtils.readObjectFromClassPathResource;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.stringContainsInOrder;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.assertj.core.api.Assertions;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpMethod;
import org.springframework.integration.test.context.SpringIntegrationTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.client.MockRestServiceServer;

import com.icegreen.greenmail.util.GreenMailUtil;
import com.obj.nc.BaseIntegrationTest;
import com.obj.nc.SystemPropertyActiveProfileResolver;
import com.obj.nc.domain.event.GenericEvent;
import com.obj.nc.osk.domain.incidentTicket.IncidentTicketOutageStartEventDto;
import com.obj.nc.osk.flows.FlowsConfig;
import com.obj.nc.osk.functions.processors.sms.OskSmsSenderRestImpl;
import com.obj.nc.osk.functions.processors.sms.config.OskSmsSenderConfigProperties;
import com.obj.nc.osk.functions.processors.sms.dtos.OskSendSmsResponseDto;
import com.obj.nc.repositories.GenericEventRepository;
import com.obj.nc.utils.JsonUtils;

@ActiveProfiles(value = { "test" }, resolver = SystemPropertyActiveProfileResolver.class)
@SpringIntegrationTest
public class OskFlowsFullUCWithLATest extends BaseIntegrationTest {
    
    @Autowired private GenericEventRepository genEventRepo;
    @Qualifier("nc.emailTemplateFormatter.messageSource")
    @Autowired private MessageSource emailMessageSource;
    @Autowired private OskSmsSenderRestImpl smsSenderRestImpl;
    @Autowired private OskSmsSenderConfigProperties properties;
    private MockRestServiceServer mockServer;
  
	@Test
    void testOutageStartWithLAsNotConfiguredAreNotNotified() throws MessagingException {
    	purgeNotifTables();
    	
		createRestCallExpectationsFor1OutageSmsFromLASegment();
		
        // GIVEN
    	GenericEvent event = readTestEventForLA();

    	//WHEN
    	genEventRepo.save(event);
    	
    	//THEN
        boolean success = greenMail.waitForIncomingEmail(10000, 3); //en+sk
        
        Assertions.assertThat(success).isTrue();
        
        MimeMessage[] messages = greenMail.getReceivedMessages();
        Assertions.assertThat(messages.length).isEqualTo(3);
       
        System.out.println(GreenMailUtil.getWholeMessage(messages[0]));
        
        assertMessagesSendTo(messages,"dysko@objectify.sk", 0); //should get filter out
        assertMessagesSendTo(messages,"cuzy@objectify.sk", 2);
        assertMessagesSendTo(messages, "sales@objectify.sk", 1);
    }
	
	private void createRestCallExpectationsFor1OutageSmsFromLASegment() {
		//Toto bude volat SMS sender
		mockServer = MockRestServiceServer.bindTo(smsSenderRestImpl.getSmsRestTemplate()).build();
    	String fullRequestUrl = properties.getGapApiUrl() + OskSmsSenderRestImpl.SEND_PATH.replace("{senderAddress}", properties.getSenderAddress());
    	
    	OskSendSmsResponseDto gapResponce = OskFlowsFullUCTest.createResponse(properties.getSenderAddress());
    	mockServer
			.expect(requestTo(fullRequestUrl))
			.andExpect(method(HttpMethod.POST))
			.andExpect(jsonPath("$.message", stringContainsInOrder("Vážený zákazník Objectify, s.r.o.", "VPS(SN:0918186997)")))
			.andExpect(jsonPath("$.address", contains(Matchers.equalTo("0918186997"))))
			.andRespond(
					withSuccess(
						JsonUtils.writeObjectToJSONString(gapResponce), APPLICATION_JSON_UTF8));
	}
	
	public static GenericEvent readTestEventForLA() {
		IncidentTicketOutageStartEventDto inputEvent = readObjectFromClassPathResource("siaNotificationEvents/outage-start-event-for-LA.json", IncidentTicketOutageStartEventDto.class);
    	GenericEvent event = GenericEvent.from(JsonUtils.writeObjectToJSONNode(inputEvent));
    	event.setFlowId(FlowsConfig.OUTAGE_START_FLOW_ID);
    	event.setExternalId(inputEvent.getId().toString());
		return event;
	}
    
}
