package com.obj.nc;

import com.icegreen.greenmail.store.FolderException;
import com.icegreen.greenmail.util.GreenMail;
import com.obj.nc.domain.event.Event;
import com.obj.nc.functions.sources.eventGenerator.EventGeneratorMicroService;
import com.obj.nc.utils.GreenMailManager;
import com.obj.nc.utils.JsonUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import javax.mail.internet.MimeMessage;

@ActiveProfiles(value = "test", resolver = SystemPropertyActiveProfileResolver.class)
@SpringBootTest
public class OskFlowsTest {

    @Autowired
    private EventGeneratorMicroService microService;

    @Autowired
    private GreenMailManager greenMailManager;

    @BeforeEach
    void cleanGreenMailMailBoxes() throws FolderException {
        greenMailManager.getGreenMail().purgeEmailFromAllMailboxes();
    }

    @Test
    void testProcessEventFromFile() {
        // GIVEN
        Event inputEvent = JsonUtils.readObjectFromClassPathResource("eventsQueue/job_event.json", Event.class);
        microService.onNext(inputEvent);

        // WHEN
        GreenMail gm = greenMailManager.getGreenMail();
        gm.waitForIncomingEmail(3);

        // THEN
        MimeMessage[] messages = gm.getReceivedMessages();
        Assertions.assertThat( messages.length ).isEqualTo(3);
    }

}

