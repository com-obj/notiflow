package com.obj.nc.osk;

import static com.obj.nc.osk.config.FlowsConfig.OUTAGE_START_FLOW_ID;
import static com.obj.nc.utils.JsonUtils.readObjectFromClassPathResource;

import java.io.IOException;
import java.util.Arrays;

import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.test.context.SpringIntegrationTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import com.icegreen.greenmail.store.FolderException;
import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.GreenMailUtil;
import com.obj.nc.BaseIntegrationTest;
import com.obj.nc.OskFlowsApplication;
import com.obj.nc.SystemPropertyActiveProfileResolver;
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
    
    @BeforeEach
    void cleanGreenMailMailBoxes() throws FolderException, IOException {
        greenMailManager.getGreenMail().purgeEmailFromAllMailboxes();
        
        jdbcTemplate.batchUpdate("delete from nc_processing_info");
        jdbcTemplate.batchUpdate("delete from nc_endpoint_processing");
        jdbcTemplate.batchUpdate("delete from nc_endpoint");        
        jdbcTemplate.batchUpdate("delete from nc_input");       
    }

    @Test
    void testProcessEventFromFile() {
        // GIVEN
    	IncidentTicketNotificationEventDto inputEvent = readObjectFromClassPathResource("siaNotificationEvents/event-full.json", IncidentTicketNotificationEventDto.class);
    	GenericEvent event = GenericEvent.from(JsonUtils.writeObjectToJSONNode(inputEvent));
    	event.setFlowId(OUTAGE_START_FLOW_ID);

    	//WHEN
    	genEventRepo.save(event);
    	
    	//THEN
        GreenMail gm = greenMailManager.getGreenMail();
        boolean success = gm.waitForIncomingEmail(6);
        
        Assertions.assertThat(success).isTrue();
        
        MimeMessage[] msgs = gm.getReceivedMessages();
        assertMessagesContains(msgs, MailMessageForAssertions.as("cuzy@objectify.sk", "Vase sluzby mozu byt nedostupne"));
        assertMessagesContains(msgs, MailMessageForAssertions.as("jancuzy@gmail.com", "Vase sluzby mozu byt nedostupne"));
        assertMessagesContains(msgs, MailMessageForAssertions.as("dysko@objectify.sk", "Vase sluzby mozu byt nedostupne"));
        
        assertMessagesContains(msgs, MailMessageForAssertions.as("slavkovsky@orange.sk", "Tvoji zakaznici maju problem"));
        assertMessagesContains(msgs, MailMessageForAssertions.as("sales@orange.sk", "Tvoji zakaznici maju problem"));
        assertMessagesContains(msgs, MailMessageForAssertions.as("hahn@orange.sk", "Tvoji zakaznici maju problem"));
        
        assertMessageCount(msgs, "cuzy@objectify.sk", 1);
        assertMessageCount(msgs, "jancuzy@gmail.com", 1);
        assertMessageCount(msgs, "dysko@objectify.sk", 1);
        
        assertMessageCount(msgs, "slavkovsky@orange.sk", 1);
        assertMessageCount(msgs, "sales@orange.sk", 1);
        assertMessageCount(msgs, "hahn@orange.sk", 1);
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
        boolean success = gm.waitForIncomingEmail(1);
        
        Assertions.assertThat(success).isTrue();
        
        MimeMessage[] messages = gm.getReceivedMessages();
        Assertions.assertThat( messages.length ).isEqualTo(1);
        Assertions.assertThat( ((InternetAddress) messages[0].getAllRecipients()[0]).getAddress() ).isEqualTo("cuzy@objectify.sk");
    }


    
}

