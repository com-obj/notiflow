package com.obj.nc.osk;

import javax.mail.internet.MimeMessage;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;

import com.icegreen.greenmail.util.GreenMailUtil;
import com.obj.nc.BaseIntegrationTest;
import com.obj.nc.SystemPropertyActiveProfileResolver;
import com.obj.nc.domain.event.GenericEvent;
import com.obj.nc.osk.functions.NotificationEventConverterProcessingFunctionTest;
import com.obj.nc.repositories.GenericEventRepository;

@ActiveProfiles(value = { "test"}, resolver = SystemPropertyActiveProfileResolver.class)
@SpringBootTest(properties = {
		"nc.flows.test-mode.enabled=true", 
		"spring.main.allow-bean-definition-overriding=true",
		"nc.flows.test-mode.period-in-seconds=1",
		"nc.flows.test-mode.recipients=cuzy@objectify.sk"})
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
public class OskFlowsTestModeTest extends BaseIntegrationTest {
    
    @Autowired
    private GenericEventRepository genEventRepo;
        
    @BeforeEach
    void cleanTables() {
        purgeNotifTables();     
    }
	
    @Test
    void testNotifyCustomersViaTestmodeEmail() {
        // GIVEN
    	GenericEvent event = NotificationEventConverterProcessingFunctionTest.readOutageStartEvent();

    	//WHEN
    	genEventRepo.save(event);
    	
    	//THEN
        boolean success = greenMail.waitForIncomingEmail(30000, 1);
        
        Assertions.assertThat(success).isTrue();
        
        MimeMessage[] msgs = greenMail.getReceivedMessages();
        Assertions.assertThat(msgs.length).isEqualTo(1);
        
        System.out.println(GreenMailUtil.getWholeMessage(msgs[0]));

        assertMessagesContains(msgs, MailMessageForAssertions.as("cuzy@objectify.sk", "Notifications digest while running test mode",
        		"Processed with love by Notification Center by Objectify", //check if translations work
        		"Vase sluzby mozu byt nedostupne", "Your services could be affected", "Zakaznici maju problem", 
        		"cuzy@objectify.sk", "jancuzy@gmail.com", "sales@objectify.sk"/*CS Agent*/, "sales@orange.sk", "hahn@orange.sk",
        		"dysko@objectify.sk", "nem_fukas@artin.sk", "slavkovsky@orange.sk",
        		//SMSs
        		"Na tuto SMS neodpovedajte.", "VPS(SN:0918186997)", "0918186997", "+421918186997", "0918186998"
        		)
        );
    }
    
}

