/*
 *   Copyright (C) 2021 the original author or authors.
 *
 *   This file is part of Notiflow
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Lesser General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public License
 *   along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.obj.nc.flows.messageProcessing;

import com.icegreen.greenmail.configuration.GreenMailConfiguration;
import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.ServerSetupTest;
import com.obj.nc.domain.event.GenericEvent;
import com.obj.nc.domain.message.EmailMessage;
import com.obj.nc.functions.processors.deliveryInfo.domain.DeliveryInfo;
import com.obj.nc.repositories.GenericEventRepository;
import com.obj.nc.repositories.GenericEventRepositoryTest;
import com.obj.nc.testUtils.BaseIntegrationTest;
import com.obj.nc.testUtils.SystemPropertyActiveProfileResolver;
import com.obj.nc.utils.JsonUtils;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.integration.test.context.SpringIntegrationTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

import static com.obj.nc.flows.inputEventRouting.config.InputEventRoutingFlowConfig.GENERIC_EVENT_CHANNEL_ADAPTER_BEAN_NAME;

@ActiveProfiles(value = "test", resolver = SystemPropertyActiveProfileResolver.class)
@SpringIntegrationTest(noAutoStartup = GENERIC_EVENT_CHANNEL_ADAPTER_BEAN_NAME)
@SpringBootTest(properties = {
    "nc.delivery-options.jsonStorePathAndFileName=src/test/resources/deliver-options/delivery-options-setting2.json", 
})
public class MessageProcessingTest extends BaseIntegrationTest {

    @Autowired
    MessageProcessingFlow msgFlow;

    @Autowired
    GenericEventRepository eventRepo;


    @RegisterExtension
    protected static GreenMailExtension greenMail = new GreenMailExtension(ServerSetupTest.SMTP)
            .withConfiguration(
                    GreenMailConfiguration.aConfig()
                            .withUser("no-reply@objectify.sk", "xxx"))
            .withPerMethodLifecycle(true);


    @BeforeEach
    void setUp(@Autowired JdbcTemplate jdbcTemplate) {
        purgeNotifTables(jdbcTemplate);
    }

    @Test
    void testSendMessage() {
        // given
        GenericEvent event = GenericEventRepositoryTest.createProcessedEvent();
        UUID eventId = eventRepo.save(event).getId();

        String INPUT_JSON_FILE = "messages/e_email_message_2_many.json";
        EmailMessage msg = JsonUtils.readObjectFromClassPathResource(INPUT_JSON_FILE, EmailMessage.class);
        msg.addPreviousEventId(eventId);

        // when
        msgFlow.processMessage(msg);

        //THEN check processing deliveryInfo
        awaitSent(eventId, 2, Duration.ofSeconds(15));
    }

    @Test
    void testEventIsFilteredOutBecauseSpamPrevention() {
        String INPUT_JSON_FILE = "messages/email_message_spam_prevention.json";
        EmailMessage msg = JsonUtils.readObjectFromClassPathResource(INPUT_JSON_FILE, EmailMessage.class);

        msgFlow.processMessage(msg);

        Awaitility.await().atMost(Duration.ofSeconds(15)).until(() -> {
            List<DeliveryInfo> info = deliveryInfoRepo.findByMessageIdAndStatusOrderByProcessedOn(msg.getId(), DeliveryInfo.DELIVERY_STATUS.DISCARDED);
            return info.size() == 1;
        });
    }
}
