package com.obj.nc.osk;

import static com.obj.nc.osk.config.FlowsConfig.OUTAGE_START_FLOW_ID;
import static com.obj.nc.utils.JsonUtils.readObjectFromClassPathResource;

import java.io.IOException;

import javax.mail.internet.MimeMessage;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.test.context.SpringIntegrationTest;
import org.springframework.jdbc.core.JdbcTemplate;
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

@ActiveProfiles(value = {"testmode"}, resolver = SystemPropertyActiveProfileResolver.class)
@SpringIntegrationTest
@ContextConfiguration(classes = OskFlowsApplication.class)
public class OskFlowsTestModeTest extends BaseIntegrationTest {
    
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
    void testNotifyCustomersViaTestmodeEmail() {
        // GIVEN
    	IncidentTicketNotificationEventDto inputEvent = readObjectFromClassPathResource("siaNotificationEvents/event-full.json", IncidentTicketNotificationEventDto.class);
    	GenericEvent event = GenericEvent.from(JsonUtils.writeObjectToJSONNode(inputEvent));
    	event.setFlowId(OUTAGE_START_FLOW_ID);

    	//WHEN
    	genEventRepo.save(event);
    	
    	//THEN
        GreenMail gm = greenMailManager.getGreenMail();
        boolean success = gm.waitForIncomingEmail(10000, 1);
        
        Assertions.assertThat(success).isTrue();
        
        MimeMessage[] msgs = gm.getReceivedMessages();
        Assertions.assertThat(msgs.length).isEqualTo(1);
        System.out.println(GreenMailUtil.getWholeMessage(msgs[0]));

    }
    
}

