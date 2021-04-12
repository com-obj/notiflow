package com.obj.nc.osk;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.stringContainsInOrder;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.util.List;
import java.util.Locale;

import javax.mail.internet.MimeMessage;

import org.assertj.core.api.Assertions;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
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
import com.icegreen.greenmail.util.GreenMailUtil;
import com.icegreen.greenmail.util.ServerSetupTest;
import com.obj.nc.BaseIntegrationTest;
import com.obj.nc.SystemPropertyActiveProfileResolver;
import com.obj.nc.domain.event.GenericEvent;
import com.obj.nc.osk.functions.processors.eventConverter.NotificationEventConverterProcessingFunctionTest;
import com.obj.nc.osk.functions.processors.sms.OskSmsSenderRestImpl;
import com.obj.nc.osk.functions.processors.sms.config.OskSmsSenderConfigProperties;
import com.obj.nc.osk.functions.processors.sms.dtos.OskSendSmsResponseDto;
import com.obj.nc.osk.functions.processors.sms.dtos.SendSmsResourceReferenceDto;
import com.obj.nc.repositories.GenericEventRepository;
import com.obj.nc.utils.JsonUtils;

@ActiveProfiles(value = { "test" }, resolver = SystemPropertyActiveProfileResolver.class)
@TestMethodOrder(OrderAnnotation.class)
@SpringBootTest
@DirtiesContext //have to dispose test mode green mail server
public class OskFlowsFullUCTest extends BaseIntegrationTest {
    
    @Autowired private GenericEventRepository genEventRepo;
    @Qualifier("nc.emailTemplateFormatter.messageSource")
    @Autowired private MessageSource emailMessageSource;
    @Autowired private OskSmsSenderRestImpl smsSenderRestImpl;
    @Autowired private OskSmsSenderConfigProperties properties;
    private MockRestServiceServer mockServer;
    @Autowired JdbcTemplate jdbcTemplate;
    

    @BeforeEach
    void redirectRestTemplate() throws FolderException {
		//Toto bude volat SMS sender
    	mockServer = MockRestServiceServer.bindTo(smsSenderRestImpl.getRestTemplate()).build();
    	greenMail.purgeEmailFromAllMailboxes();
    }

    @Test
    @Order(1)  
    void testOutageStart() {
    	purgeNotifTables(jdbcTemplate);
    	
    	createRestCallExpectationsForOutageStartSms();
    	
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
        
        assertMessagesContent(msgs, "start");
        
        //check SMS send via RestCall 
        mockServer.verify();
    }
    
    @Test
    @Order(2)  
    void testOutageEnd() {
    	createRestCallExpectationsForOutageEndSms();
    	
        // GIVEN
    	GenericEvent event = NotificationEventConverterProcessingFunctionTest.readOutageEndEvent();

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
        
        assertMessagesContent(msgs, "end");
        
        //check SMS send via RestCall 
        mockServer.verify();
    }
    
    private String getMsg(String key, Locale loc) {
    	return emailMessageSource.getMessage(key, null, loc);
    }

	private void assertMessagesContent(MimeMessage[] msgs, String startEnd) {
		MimeMessage msg = assertMessagesContains(msgs, MailMessageForAssertions.as("cuzy@objectify.sk", 
        		emailMessageSource.getMessage("cust."+startEnd+".subject", null, Locale.US), 
        		"Objectify, s.r.o","0918186997", "VPS sifrovana", "Mocidla 249, Myto pod Dumbierom"
        		)
        );
        System.out.println(GreenMailUtil.getWholeMessage(msg));
        assertMessagesContains(msgs, MailMessageForAssertions.as("jancuzy@gmail.com", getMsg("cust."+startEnd+".subject", Locale.US), "Objectify, s.r.o"));
        assertMessagesContains(msgs, MailMessageForAssertions.as("dysko@objectify.sk", getMsg("cust."+startEnd+".subject", Locale.US), "Objectify, s.r.o"));
        
        assertMessagesContains(msgs, MailMessageForAssertions.as("cuzy@objectify.sk", getMsg("cust."+startEnd+".subject", new Locale("sk")), "Objectify, s.r.o"));
        assertMessagesContains(msgs, MailMessageForAssertions.as("jancuzy@gmail.com", getMsg("cust."+startEnd+".subject", new Locale("sk")), "Objectify, s.r.o"));
        assertMessagesContains(msgs, MailMessageForAssertions.as("dysko@objectify.sk", getMsg("cust."+startEnd+".subject", new Locale("sk")), "Objectify, s.r.o"));
        
        //sales
        //only slovak
        List<MimeMessage> lMsgs = assertMessageCount(msgs, "slavkovsky@orange.sk", 1);
        System.out.println(GreenMailUtil.getWholeMessage(lMsgs.iterator().next()));
        
        assertMessageCount(msgs, "sales@orange.sk", 1);
        assertMessageCount(msgs, "hahn@orange.sk", 1);
        
        if (startEnd.equals("start")) {
	        msg = assertMessagesContains(msgs, MailMessageForAssertions.as("slavkovsky@orange.sk", 
	        		getMsg("sales."+ startEnd +".subject", new Locale("sk")),
	        		"Objectify, s.r.o","obj","0918186997", "VPS sifrovana", 
	        		"Mocidla 249, Myto pod Dumbierom","Martinengova 4881/36 811 02 Bratislava",
	        		"Artin, s.r.o.","Artin","0918186998", "VPS sifrovana/nesifrovana", 
	        		"Westend tower","Dubravska cesta 2 841 04 Bratislava",
	        		"0918186999"
	        		)
	        );
        } else {
	        msg = assertMessagesContains(msgs, MailMessageForAssertions.as("slavkovsky@orange.sk", 
	        		getMsg("sales."+ startEnd +".subject", new Locale("sk")),
	        		"sme o tom informovali."
	        		)
	        );
        }
        System.out.println(GreenMailUtil.getWholeMessage(msg));
        assertMessagesContains(msgs, MailMessageForAssertions.as("sales@orange.sk", getMsg("sales."+ startEnd +".subject", new Locale("sk"))));
        assertMessagesContains(msgs, MailMessageForAssertions.as("hahn@orange.sk", getMsg("sales."+ startEnd +".subject", new Locale("sk"))));

        //sales agent
        //only slovak
        lMsgs = assertMessageCount(msgs, "sales@objectify.sk", 1);
        System.out.println(GreenMailUtil.getWholeMessage(lMsgs.iterator().next()));
        
        msg = assertMessagesContains(msgs, MailMessageForAssertions.as("sales@objectify.sk", getMsg("salesAgent."+ startEnd +".subject", new Locale("sk")),
        		"Objectify, s.r.o","obj","0918186997", "VPS sifrovana", 
        		"Mocidla 249, Myto pod Dumbierom", "Martinengova 4881/36 811 02 Bratislava",
        		"Artin, s.r.o.","Artin","0918186998", "VPS sifrovana/nesifrovana", 
        		"Westend tower","Dubravska cesta 2 841 04 Bratislava",
        		"0918186999"
        		)
        );
	}
    
    private void createRestCallExpectation(String recieverNumber, String ... messageParts ) {
    	String fullRequestUrl = 
    			properties.getGapApiUrl() 
    			+ OskSmsSenderRestImpl.SEND_PATH.replace("{senderAddress}", properties.getSenderAddress());
    	
    	OskSendSmsResponseDto gapResponce = createResponse(properties.getSenderAddress());
    		
    	mockServer
			.expect(requestTo(fullRequestUrl))
			.andExpect(method(HttpMethod.POST))
			.andExpect(jsonPath("$.message", stringContainsInOrder(messageParts)))
			.andExpect(jsonPath("$.address", contains(Matchers.equalTo(recieverNumber))))
			.andRespond(
					withSuccess(
						JsonUtils.writeObjectToJSONString(gapResponce), APPLICATION_JSON_UTF8));
    }

	private void createRestCallExpectationsForOutageStartSms() {
		//TODO: toto je asi vcelku brittle,.. nemyslim, ze viem garantovat poradie
		createRestCallExpectation("0918186997", "Vážený zákazník Objectify, s.r.o.", "sme zaznamenali výpadok", "VPS(SN:0918186997)", "VPS sifrovana(SN:0918186997)");
		createRestCallExpectation("+421918186997", "Vážený zákazník Objectify, s.r.o.", "sme zaznamenali výpadok", "VPS(SN:0918186997)", "VPS sifrovana(SN:0918186997)");
		createRestCallExpectation("0918186998", "Vážený zákazník Artin, s.r.o.", "sme zaznamenali výpadok", "VPS sifrovana/nesifrovana(SN:0918186998)", "VPS sifrovana(SN:0918186999)");
	}
	
	private void createRestCallExpectationsForOutageEndSms() {
		//TODO: toto je asi vcelku brittle,.. nemyslim, ze viem garantovat poradie
		createRestCallExpectation("0918186997", "Vážený zákazník Objectify, s.r.o.", "sme odstranili vypadok", "VPS(SN:0918186997)", "VPS sifrovana(SN:0918186997)");
		createRestCallExpectation("+421918186997", "Vážený zákazník Objectify, s.r.o.", "sme odstranili vypadok", "VPS(SN:0918186997)", "VPS sifrovana(SN:0918186997)");
		createRestCallExpectation("0918186998", "Vážený zákazník Artin, s.r.o.", "sme odstranili vypadok", "VPS sifrovana/nesifrovana(SN:0918186998)", "VPS sifrovana(SN:0918186999)");
	}
	
    public static OskSendSmsResponseDto createResponse(String senderAddrress) {
        OskSendSmsResponseDto response = new OskSendSmsResponseDto();
        SendSmsResourceReferenceDto resourceReference = new SendSmsResourceReferenceDto();
        String resourceUrl = "/"
//                .concat(request.getSenderAddress())
        		.concat(senderAddrress)
                .concat("/requests/")
//                .concat(request.getAddress().get(0))
                .concat("phonenumber")
                .concat("-SUCCESS-0000000000000000000000000000000000000478#");
        resourceReference.setResourceURL(resourceUrl);
        response.setResourceReference(resourceReference);
        return response;
    }
    
    @RegisterExtension
    protected static GreenMailExtension greenMail = new GreenMailExtension(ServerSetupTest.SMTP)
      	.withConfiguration(
      			GreenMailConfiguration.aConfig()
      			.withUser("no-reply@objectify.sk", "xxx"))
      	.withPerMethodLifecycle(true);
    
}

