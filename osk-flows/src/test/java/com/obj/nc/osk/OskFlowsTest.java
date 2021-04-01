package com.obj.nc.osk;

import static com.obj.nc.utils.JsonUtils.readObjectFromClassPathResource;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.stringContainsInOrder;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.assertj.core.api.Assertions;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpMethod;
import org.springframework.integration.test.context.SpringIntegrationTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.client.MockRestServiceServer;

import com.icegreen.greenmail.store.FolderException;
import com.icegreen.greenmail.util.GreenMailUtil;
import com.obj.nc.BaseIntegrationTest;
import com.obj.nc.SystemPropertyActiveProfileResolver;
import com.obj.nc.domain.event.GenericEvent;
import com.obj.nc.osk.domain.incidentTicket.IncidentTicketOutageStartEventDto;
import com.obj.nc.osk.flows.FlowsConfig;
import com.obj.nc.osk.functions.NotificationEventConverterProcessingFunctionTest;
import com.obj.nc.osk.functions.processors.sms.OskSmsSenderRestImpl;
import com.obj.nc.osk.functions.processors.sms.config.OskSmsSenderConfigProperties;
import com.obj.nc.osk.functions.processors.sms.dtos.OskSendSmsResponseDto;
import com.obj.nc.osk.functions.processors.sms.dtos.SendSmsResourceReferenceDto;
import com.obj.nc.repositories.GenericEventRepository;
import com.obj.nc.utils.JsonUtils;

@ActiveProfiles(value = { "test" }, resolver = SystemPropertyActiveProfileResolver.class)
@SpringIntegrationTest
public class OskFlowsTest extends BaseIntegrationTest {
    
    @Autowired private GenericEventRepository genEventRepo;
    @Autowired private JdbcTemplate jdbcTemplate;
    @Qualifier("nc.emailTemplateFormatter.messageSource")
    @Autowired private MessageSource emailMessageSource;
    @Autowired private OskSmsSenderRestImpl smsSenderRestImpl;
    @Autowired private OskSmsSenderConfigProperties properties;
    private MockRestServiceServer mockServer;
    
    @BeforeEach
    void purgeNotifTables() throws FolderException, IOException {
        jdbcTemplate.batchUpdate("delete from nc_processing_info");
        jdbcTemplate.batchUpdate("delete from nc_endpoint_processing");
        jdbcTemplate.batchUpdate("delete from nc_endpoint");        
        jdbcTemplate.batchUpdate("delete from nc_input");              
    }
    
    @Before
    public void init() {
        
    }

    @Test
    void testNotifyCustomersAndSalesByEmail() {
    	createRestCallExpectationsFor3OutageSms();
    	
        // GIVEN
    	GenericEvent event = NotificationEventConverterProcessingFunctionTest.readOutageStartEvent();

    	//WHEN
    	genEventRepo.save(event);
    	
    	//THEN
        boolean success = greenMail.waitForIncomingEmail(20000L, 12);
        
        Assertions.assertThat(success).isTrue();
        
        MimeMessage[] msgs = greenMail.getReceivedMessages();
        Assertions.assertThat(msgs.length).isEqualTo(12); //4xcustomers(en/sk), 3xsales, 1xsales agent 
        System.out.println(GreenMailUtil.getWholeMessage(msgs[0]));
        
        //customers
        //slovak/english
        assertMessageCount(msgs, "cuzy@objectify.sk", 2);
        assertMessageCount(msgs, "jancuzy@gmail.com", 2);
        assertMessageCount(msgs, "dysko@objectify.sk", 2);
        assertMessageCount(msgs, "nem_fukas@artin.sk", 2);
        
        MimeMessage msg = assertMessagesContains(msgs, MailMessageForAssertions.as("cuzy@objectify.sk", 
        		emailMessageSource.getMessage("cust.start.subject", null, Locale.US), 
        		"Objectify, s.r.o","0918186997", "VPS sifrovana", "Mocidla 249, Myto pod Dumbierom"
        		)
        );
        System.out.println(GreenMailUtil.getWholeMessage(msg));
        assertMessagesContains(msgs, MailMessageForAssertions.as("jancuzy@gmail.com", emailMessageSource.getMessage("cust.start.subject", null, Locale.US), "Objectify, s.r.o"));
        assertMessagesContains(msgs, MailMessageForAssertions.as("dysko@objectify.sk", emailMessageSource.getMessage("cust.start.subject", null, Locale.US), "Objectify, s.r.o"));
        
        assertMessagesContains(msgs, MailMessageForAssertions.as("cuzy@objectify.sk", emailMessageSource.getMessage("cust.start.subject", null, new Locale("sk")), "Objectify, s.r.o"));
        assertMessagesContains(msgs, MailMessageForAssertions.as("jancuzy@gmail.com", emailMessageSource.getMessage("cust.start.subject", null, new Locale("sk")), "Objectify, s.r.o"));
        assertMessagesContains(msgs, MailMessageForAssertions.as("dysko@objectify.sk", emailMessageSource.getMessage("cust.start.subject", null, new Locale("sk")), "Objectify, s.r.o"));
        
        //sales
        //only slovak
        List<MimeMessage> lMsgs = assertMessageCount(msgs, "slavkovsky@orange.sk", 1);
        System.out.println(GreenMailUtil.getWholeMessage(lMsgs.iterator().next()));
        
        assertMessageCount(msgs, "sales@orange.sk", 1);
        assertMessageCount(msgs, "hahn@orange.sk", 1);
        
        msg = assertMessagesContains(msgs, MailMessageForAssertions.as("slavkovsky@orange.sk", "Tvoji zakaznici maju problem",
        		"Objectify, s.r.o","obj","0918186997", "VPS sifrovana", 
        		"Mocidla 249, Myto pod Dumbierom","Martinengova 4881/36 811 02 Bratislava",
        		"Artin, s.r.o.","Artin","0918186998", "VPS sifrovana/nesifrovana", 
        		"Westend tower","Dubravska cesta 2 841 04 Bratislava",
        		"0918186999"
        		)
        );
        System.out.println(GreenMailUtil.getWholeMessage(msg));
        assertMessagesContains(msgs, MailMessageForAssertions.as("sales@orange.sk", "Tvoji zakaznici maju problem"));
        assertMessagesContains(msgs, MailMessageForAssertions.as("hahn@orange.sk", "Tvoji zakaznici maju problem"));

        //sales agent
        //only slovak
        lMsgs = assertMessageCount(msgs, "sales@objectify.sk", 1);
        System.out.println(GreenMailUtil.getWholeMessage(lMsgs.iterator().next()));
        
        msg = assertMessagesContains(msgs, MailMessageForAssertions.as("sales@objectify.sk", "Zakaznici maju problem",
        		"Objectify, s.r.o","obj","0918186997", "VPS sifrovana", 
        		"Mocidla 249, Myto pod Dumbierom", "Martinengova 4881/36 811 02 Bratislava",
        		"Artin, s.r.o.","Artin","0918186998", "VPS sifrovana/nesifrovana", 
        		"Westend tower","Dubravska cesta 2 841 04 Bratislava",
        		"0918186999"
        		)
        );
        
        //check SMS send via RestCall 
        mockServer.verify();

    }

	private void createRestCallExpectationsFor3OutageSms() {
		//Toto bude volat SMS sender
		mockServer = MockRestServiceServer.bindTo(smsSenderRestImpl.getSmsRestTemplate()).build();
    	String fullRequestUrl = properties.getGapApiUrl() + OskSmsSenderRestImpl.SEND_PATH.replace("{senderAddress}", properties.getSenderAddress());
    	
    	//TODO: toto je asi vcelku brittle,.. nemyslim, ze viem garantovat poradie
    	mockServer
			.expect(requestTo(fullRequestUrl))
			.andExpect(method(HttpMethod.POST))
			.andExpect(jsonPath("$.message", stringContainsInOrder("Vážený zákazník Objectify, s.r.o.", "VPS(SN:0918186997)", "VPS sifrovana(SN:0918186997)")))
			.andExpect(jsonPath("$.address", contains(Matchers.equalTo("0918186997"))))
			.andRespond(
					withSuccess(
						JsonUtils.writeObjectToJSONString(createResponse()), APPLICATION_JSON_UTF8));
    	
    	mockServer
			.expect(requestTo(fullRequestUrl))
			.andExpect(method(HttpMethod.POST))
			.andExpect(jsonPath("$.message", stringContainsInOrder("Vážený zákazník Objectify, s.r.o.", "VPS(SN:0918186997)", "VPS sifrovana(SN:0918186997)")))
			.andExpect(jsonPath("$.address", contains(Matchers.equalTo("+421918186997"))))
			.andRespond(
				withSuccess(
					JsonUtils.writeObjectToJSONString(createResponse()), APPLICATION_JSON_UTF8));
    	
    	mockServer
			.expect(requestTo(fullRequestUrl))
			.andExpect(method(HttpMethod.POST))
			.andExpect(jsonPath("$.message", stringContainsInOrder("Vážený zákazník Artin, s.r.o.", "VPS sifrovana/nesifrovana(SN:0918186998)", "VPS sifrovana(SN:0918186999)")))
			.andExpect(jsonPath("$.address", contains(Matchers.equalTo("0918186998"))))
			.andRespond(
				withSuccess(
					JsonUtils.writeObjectToJSONString(createResponse()), APPLICATION_JSON_UTF8));

	}

	@Test
    void testLAsNotConfiguredAreNotNotified() throws MessagingException {
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
    	
    	mockServer
			.expect(requestTo(fullRequestUrl))
			.andExpect(method(HttpMethod.POST))
			.andExpect(jsonPath("$.message", stringContainsInOrder("Vážený zákazník Objectify, s.r.o.", "VPS(SN:0918186997)")))
			.andExpect(jsonPath("$.address", contains(Matchers.equalTo("0918186997"))))
			.andRespond(
					withSuccess(
						JsonUtils.writeObjectToJSONString(createResponse()), APPLICATION_JSON_UTF8));
	}
	
	public static GenericEvent readTestEventForLA() {
		IncidentTicketOutageStartEventDto inputEvent = readObjectFromClassPathResource("siaNotificationEvents/outage-start-event-for-LA.json", IncidentTicketOutageStartEventDto.class);
    	GenericEvent event = GenericEvent.from(JsonUtils.writeObjectToJSONNode(inputEvent));
    	event.setFlowId(FlowsConfig.OUTAGE_START_FLOW_ID);
    	event.setExternalId(inputEvent.getId().toString());
		return event;
	}
	
    private OskSendSmsResponseDto createResponse() {
        OskSendSmsResponseDto response = new OskSendSmsResponseDto();
        SendSmsResourceReferenceDto resourceReference = new SendSmsResourceReferenceDto();
        String resourceUrl = "/"
//                .concat(request.getSenderAddress())
        		.concat(properties.getSenderAddress())
                .concat("/requests/")
//                .concat(request.getAddress().get(0))
                .concat("phonenumber")
                .concat("-SUCCESS-0000000000000000000000000000000000000478#");
        resourceReference.setResourceURL(resourceUrl);
        response.setResourceReference(resourceReference);
        return response;
    }
    
}

