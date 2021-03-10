package com.obj.nc;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.mail.internet.MimeMessage;

import org.assertj.core.api.Assertions;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.core.MessageSource;
import org.springframework.integration.test.context.MockIntegrationContext;
import org.springframework.integration.test.context.SpringIntegrationTest;
import org.springframework.integration.test.util.OnlyOnceTrigger;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.scheduling.Trigger;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import com.icegreen.greenmail.store.FolderException;
import com.icegreen.greenmail.util.GreenMail;
import com.obj.nc.domain.ProcessingInfo;
import com.obj.nc.domain.event.Event;
import com.obj.nc.utils.GreenMailManager;
import com.obj.nc.utils.JsonUtils;

@ActiveProfiles(value = "test", resolver = SystemPropertyActiveProfileResolver.class)
@SpringBootTest
@SpringIntegrationTest(noAutoStartup = {"eventSupplier"})
public class OskFlowsTest {
		
	@Autowired
	private MockIntegrationContext mockIntegrationContext;
    
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
        Event inputEvent = JsonUtils.readObjectFromClassPathResource("eventsQueue/job_event.json", Event.class);
        MessageSource<Event> messageSource = () -> new GenericMessage<>(inputEvent);
    	mockIntegrationContext.substituteMessageSourceFor("eventSupplier", messageSource);

        // WHEN
        GreenMail gm = greenMailManager.getGreenMail();
        boolean success = gm.waitForIncomingEmail(3);
        
        // THEN
        Assertions.assertThat( success ).isEqualTo( true );
        MimeMessage[] messages = gm.getReceivedMessages();
        Assertions.assertThat( messages.length ).isEqualTo(3);
        
        // THEN
        List<ProcessingInfo> persistedPIs = ProcessingInfo.findProcessingInfo(inputEvent.getHeader().getId(), "SendEmail");
        Assertions.assertThat(persistedPIs.size()).isEqualTo(3);
    }
    
    @org.springframework.boot.test.context.TestConfiguration
    public static class TestConfiguration {

    	@Bean
    	public Trigger sourceTrigger() {
    		return new OnlyOnceTrigger();
    	}

    }

}

