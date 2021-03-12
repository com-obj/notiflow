package com.obj.nc;

import static com.obj.nc.utils.JsonUtils.readObjectFromClassPathResource;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import javax.mail.internet.MimeMessage;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.integration.test.context.SpringIntegrationTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import com.icegreen.greenmail.store.FolderException;
import com.icegreen.greenmail.util.GreenMail;
import com.obj.nc.domain.ProcessingInfo;
import com.obj.nc.osk.config.SIAInboundGatewayConfig;
import com.obj.nc.osk.sia.dto.IncidentTicketNotificationContactDto;
import com.obj.nc.utils.GreenMailManager;

@ActiveProfiles(value = "test", resolver = SystemPropertyActiveProfileResolver.class)
@SpringIntegrationTest
public class OskFlowsTest extends BaseIntegrationTest {
		

    
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
    }

    @Test
    @DirtiesContext
    void testProcessEventFromFile() {
        // GIVEN
    	IncidentTicketNotificationContactDto inputEvent = readObjectFromClassPathResource("siaNotificationEvents/full-valid-event.json", IncidentTicketNotificationContactDto.class);
    	
    	//WHEN
//    	ResponseEntity<String> resp = postPojoInBody(SIAInboundGatewayConfig.NOTIFICATION_EVENT_REST_ENDPOINT_URL, inputEvent, String.class);
    	
//    	//THEN
//        Assertions.assertThat( resp.getBody() ).isEqualTo( "OK" );
//
//        //THEN
//        GreenMail gm = greenMailManager.getGreenMail();
//        boolean success = gm.waitForIncomingEmail(3);
//        
//        // THEN
//        Assertions.assertThat( success ).isEqualTo( true );
//        MimeMessage[] messages = gm.getReceivedMessages();
//        Assertions.assertThat( messages.length ).isEqualTo(3);
//        
//        // THEN
//        List<ProcessingInfo> persistedPIs = ProcessingInfo.findProcessingInfo(UUID.fromString(resp.getBody()), "SendEmail");
//        Assertions.assertThat(persistedPIs.size()).isEqualTo(3);
    }


    
}

