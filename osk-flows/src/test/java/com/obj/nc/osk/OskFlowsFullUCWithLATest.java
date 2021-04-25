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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpMethod;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.client.MockRestServiceServer;

import com.icegreen.greenmail.configuration.GreenMailConfiguration;
import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.store.FolderException;
import com.icegreen.greenmail.util.ServerSetupTest;
import com.obj.nc.BaseIntegrationTest;
import com.obj.nc.SystemPropertyActiveProfileResolver;
import com.obj.nc.domain.event.GenericEvent;
import com.obj.nc.osk.domain.IncidentTicketOutageStartEventDto;
import com.obj.nc.osk.flows.FlowsConfig;
import com.obj.nc.osk.functions.processors.sms.OskSmsSenderRestImpl;
import com.obj.nc.osk.functions.processors.sms.config.OskSmsSenderConfigProperties;
import com.obj.nc.osk.functions.processors.sms.dtos.OskSendSmsResponseDto;
import com.obj.nc.repositories.GenericEventRepository;
import com.obj.nc.utils.JsonUtils;

@ActiveProfiles(value = { "test" }, resolver = SystemPropertyActiveProfileResolver.class)
@SpringBootTest
@DirtiesContext //have to dispose test mode green mail server
public class OskFlowsFullUCWithLATest extends BaseIntegrationTest {
    
    @Autowired private GenericEventRepository genEventRepo;
    @Qualifier("nc.emailTemplateFormatter.messageSource")
    @Autowired private MessageSource emailMessageSource;
    @Autowired private OskSmsSenderRestImpl smsSenderRestImpl;
    @Autowired private OskSmsSenderConfigProperties properties;
    private MockRestServiceServer mockServer;
    
    @BeforeEach
    void redirectRestTemplate(@Autowired JdbcTemplate jdbcTemplate) throws FolderException {
		//Toto bude volat SMS sender
    	mockServer = MockRestServiceServer.bindTo(smsSenderRestImpl.getRestTemplate()).build();
    	greenMail.purgeEmailFromAllMailboxes();
    	purgeNotifTables(jdbcTemplate);
    }
  
	@Test
    void testOutageStartWithLAsNotConfiguredAreNotNotified() throws MessagingException {
		createRestCallExpectationsFor1OutageSmsFromLASegment();
		
        // GIVEN
    	GenericEvent event = readTestEventForLA();

    	//WHEN
    	genEventRepo.save(event);
    	
    	//THEN
        boolean success = greenMail.waitForIncomingEmail(20000, 2); // 1.mergnute en+sk, 2.iba sk
        
        Assertions.assertThat(success).isTrue();
        
        MimeMessage[] messages = greenMail.getReceivedMessages();
        Assertions.assertThat(messages.length).isEqualTo(2);
       
        assertMessagesSendTo(messages,"dysko@objectify.sk", 0); //should get filter out
        assertMessagesSendTo(messages,"cuzy@objectify.sk", 1);
        assertMessagesSendTo(messages, "sales@objectify.sk", 1);
    }
	
	private void createRestCallExpectationsFor1OutageSmsFromLASegment() {
		//Toto bude volat SMS sender
    	String fullRequestUrl = properties.getGapApiUrl() + OskSmsSenderRestImpl.SEND_PATH.replace("{senderAddress}", properties.getSenderAddress());
    	
    	OskSendSmsResponseDto gapResponce = OskFlowsFullUCTest.createResponse(properties.getSenderAddress());
    	mockServer
			.expect(requestTo(fullRequestUrl))
			.andExpect(method(HttpMethod.POST))
			.andExpect(jsonPath("$.message", stringContainsInOrder("Vážený zákazník,", "VPS(SN:0918186997)")))
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
    
    @RegisterExtension
    protected static GreenMailExtension greenMail = new GreenMailExtension(ServerSetupTest.SMTP)
      	.withConfiguration(
      			GreenMailConfiguration.aConfig()
      			.withUser("no-reply@objectify.sk", "xxx"))
      	.withPerMethodLifecycle(true);
}

