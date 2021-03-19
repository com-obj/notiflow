package com.obj.nc.osk;

import static com.obj.nc.osk.config.FlowsConfig.OUTAGE_START_FLOW_ID;
import static com.obj.nc.utils.JsonUtils.readObjectFromClassPathResource;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;

import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.commons.jxpath.JXPathContext;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.integration.test.context.SpringIntegrationTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import com.icegreen.greenmail.store.FolderException;
import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.GreenMailUtil;
import com.obj.nc.BaseIntegrationTest;
import com.obj.nc.OskFlowsApplication;
import com.obj.nc.SystemPropertyActiveProfileResolver;
import com.obj.nc.domain.event.Event;
import com.obj.nc.domain.event.GenericEvent;
import com.obj.nc.osk.dto.IncidentTicketNotificationEventDto;
import com.obj.nc.repositories.GenericEventRepository;
import com.obj.nc.utils.GreenMailManager;
import com.obj.nc.utils.JsonUtils;

@ActiveProfiles(value = "test", resolver = SystemPropertyActiveProfileResolver.class)
@SpringIntegrationTest
@ContextConfiguration(classes = OskFlowsApplication.class)
public class OskFlowsTest extends BaseIntegrationTest {
    
    @Autowired
    private GenericEventRepository genEventRepo;
    
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private GreenMailManager greenMailManager;
    
    @Autowired
    @Qualifier("nc.emailTemplateFormatter.messageSource")
    private MessageSource emailMessageSource;
    
    @BeforeEach
    void cleanGreenMailMailBoxes() throws FolderException, IOException {
        greenMailManager.getGreenMail().purgeEmailFromAllMailboxes();
        
        jdbcTemplate.batchUpdate("delete from nc_processing_info");
        jdbcTemplate.batchUpdate("delete from nc_endpoint_processing");
        jdbcTemplate.batchUpdate("delete from nc_endpoint");        
        jdbcTemplate.batchUpdate("delete from nc_input");       
    }

    @Test
    void testNotifyCustomersAndSalesByEmail() {
        // GIVEN
    	IncidentTicketNotificationEventDto inputEvent = readObjectFromClassPathResource("siaNotificationEvents/event-full.json", IncidentTicketNotificationEventDto.class);
    	GenericEvent event = GenericEvent.from(JsonUtils.writeObjectToJSONNode(inputEvent));
    	event.setFlowId(OUTAGE_START_FLOW_ID);

    	//WHEN
    	genEventRepo.save(event);
    	
    	//THEN
        GreenMail gm = greenMailManager.getGreenMail();
        boolean success = gm.waitForIncomingEmail(10000, 12);
        
        Assertions.assertThat(success).isTrue();
        
        MimeMessage[] msgs = gm.getReceivedMessages();
        Assertions.assertThat(msgs.length).isEqualTo(12); //4xcustomers(en/sk), 3xsales, 1xsales agent 
        System.out.println(GreenMailUtil.getWholeMessage(msgs[0]));
        
        //customers
        //slovak/english
        assertMessageCount(msgs, "cuzy@objectify.sk", 2);
        assertMessageCount(msgs, "jancuzy@gmail.com", 2);
        assertMessageCount(msgs, "dysko@objectify.sk", 2);
        assertMessageCount(msgs, "fukas@artin.sk", 2);
        
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
        lMsgs = assertMessageCount(msgs, "sales.agent@orange.sk", 1);
        System.out.println(GreenMailUtil.getWholeMessage(lMsgs.iterator().next()));
        
        msg = assertMessagesContains(msgs, MailMessageForAssertions.as("sales.agent@orange.sk", "Zakaznici maju problem",
        		"Objectify, s.r.o","obj","0918186997", "VPS sifrovana", 
        		"Mocidla 249, Myto pod Dumbierom", "Martinengova 4881/36 811 02 Bratislava",
        		"Artin, s.r.o.","Artin","0918186998", "VPS sifrovana/nesifrovana", 
        		"Westend tower","Dubravska cesta 2 841 04 Bratislava",
        		"0918186999"
        		)
        );

    }

	@Test
    void testLAsNotConfiguredAreNotNotified() throws MessagingException {
        // GIVEN
    	IncidentTicketNotificationEventDto inputEvent = readObjectFromClassPathResource("siaNotificationEvents/event-for-LA.json", IncidentTicketNotificationEventDto.class);
    	GenericEvent event = GenericEvent.from(JsonUtils.writeObjectToJSONNode(inputEvent));
    	event.setFlowId(OUTAGE_START_FLOW_ID);

    	//WHEN
    	genEventRepo.save(event);
    	
    	//THEN
        GreenMail gm = greenMailManager.getGreenMail();
        boolean success = gm.waitForIncomingEmail(10000, 3); //en+sk
        
        Assertions.assertThat(success).isTrue();
        
        MimeMessage[] messages = gm.getReceivedMessages();
        Assertions.assertThat(messages.length).isEqualTo(3);
       
        System.out.println(GreenMailUtil.getWholeMessage(messages[0]));
        
        assertMessagesSendTo(messages,"dysko@objectify.sk", 0); //should get filter out
        assertMessagesSendTo(messages,"cuzy@objectify.sk", 2);
        assertMessagesSendTo(messages, "sales.agent@orange.sk", 1);
    }
    
}

