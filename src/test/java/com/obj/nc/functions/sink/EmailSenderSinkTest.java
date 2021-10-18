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

package com.obj.nc.functions.sink;

import com.icegreen.greenmail.configuration.GreenMailConfiguration;
import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.store.FolderException;
import com.icegreen.greenmail.util.GreenMailUtil;
import com.icegreen.greenmail.util.ServerSetupTest;
import com.obj.nc.domain.content.email.EmailContent;
import com.obj.nc.domain.event.GenericEvent;
import com.obj.nc.domain.message.EmailMessage;
import com.obj.nc.exceptions.PayloadValidationException;
import com.obj.nc.functions.processors.deliveryInfo.DeliveryInfoSendGenerator;
import com.obj.nc.functions.processors.deliveryInfo.domain.DeliveryInfo.DELIVERY_STATUS;
import com.obj.nc.functions.processors.senders.EmailSender;
import com.obj.nc.functions.processors.senders.config.EmailSenderConfigProperties;
import com.obj.nc.functions.processors.senders.dtos.DeliveryInfoSendResult;
import com.obj.nc.repositories.GenericEventRepository;
import com.obj.nc.repositories.GenericEventRepositoryTest;
import com.obj.nc.testUtils.BaseIntegrationTest;
import com.obj.nc.testUtils.SystemPropertyActiveProfileResolver;
import com.obj.nc.utils.JsonUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.integration.test.context.SpringIntegrationTest;
import org.springframework.test.context.ActiveProfiles;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import static com.obj.nc.flows.inputEventRouting.config.InputEventRoutingFlowConfig.GENERIC_EVENT_CHANNEL_ADAPTER_BEAN_NAME;

@ActiveProfiles(value = "test", resolver = SystemPropertyActiveProfileResolver.class)
@SpringIntegrationTest(noAutoStartup = GENERIC_EVENT_CHANNEL_ADAPTER_BEAN_NAME)
@SpringBootTest
class EmailSenderSinkTest extends BaseIntegrationTest {

//    @Autowired private JavaMailSenderImpl defaultJavaMailSender;
    @Autowired private EmailSender functionSend;
    @Autowired private DeliveryInfoSendGenerator delInfoGenerator;
    @Autowired private EmailSenderConfigProperties settings;
    @Autowired private GenericEventRepository eventRepo;
    
    @RegisterExtension
    protected static GreenMailExtension greenMail = new GreenMailExtension(ServerSetupTest.SMTP)
      	.withConfiguration(
      			GreenMailConfiguration.aConfig()
      			.withUser("no-reply@objectify.sk", "xxx"))
      	.withPerMethodLifecycle(true);

    @BeforeEach
    void setUp() throws FolderException {
    	greenMail.purgeEmailFromAllMailboxes();
    }

    @Test
    void sendSingleMail() throws MessagingException, IOException {
        //GIVEN
		GenericEvent event = GenericEventRepositoryTest.createDirectMessageEvent();
		UUID eventId1 = eventRepo.save(event).getId();
    	
        String INPUT_JSON_FILE = "messages/email_message.json";
        EmailMessage message = JsonUtils.readObjectFromClassPathResource(INPUT_JSON_FILE, EmailMessage.class);
        message.addPreviousEventId(eventId1);

        //WHEN
        message = functionSend.apply(message);
        List<DeliveryInfoSendResult> delInfos = delInfoGenerator.apply(message);

        
        Assertions.assertThat(delInfos.size()).isEqualTo(1);
        DeliveryInfoSendResult delInfo = delInfos.iterator().next();
        Assertions.assertThat(delInfo.getStatus()).isEqualTo(DELIVERY_STATUS.SENT);
        Assertions.assertThat(delInfo.getProcessedOn()).isNotNull();
        Assertions.assertThat(delInfo.getReceivingEndpoint()).isEqualTo(message.getReceivingEndpoints().get(0));
        Assertions.assertThat(delInfo.getEventIdsAsList()).isEqualTo(message.getPreviousEventIds());

        //THEN
        MimeMessage[] messages = greenMail.getReceivedMessages();
        Assertions.assertThat( messages.length ).isEqualTo(1);
        Assertions.assertThat( messages[0].getSubject() ).isEqualTo("Subject");
        Assertions.assertThat( messages[0].getFrom()[0] ).extracting("address").isEqualTo(settings.getFromMailAddress());
    }

    @Test
    void sendMailToManyRecipients() {
        //GIVEN
        String INPUT_JSON_FILE = "messages/e_email_message_2_many.json";
        EmailMessage message = JsonUtils.readObjectFromClassPathResource(INPUT_JSON_FILE, EmailMessage.class);

        //WHEN -THEN
        Assertions.assertThatThrownBy(() -> {
            functionSend.apply(message);
        })
                .isInstanceOf(PayloadValidationException.class)
                .hasMessageContaining("EmailContent sender can send to only one recipient. Found more: ");
    }

    @Test
    void sendEmailToNonEmailEnpoint() {
        //GIVEN
        String INPUT_JSON_FILE = "messages/e_email_message_2_push_endpoint.json";
        EmailMessage message = JsonUtils.readObjectFromClassPathResource(INPUT_JSON_FILE, EmailMessage.class);

        //WHEN -THEN
        Assertions.assertThatThrownBy(() -> {
            functionSend.apply(message);
        })
                .isInstanceOf(PayloadValidationException.class)
                .hasMessageContaining("EmailContent sender can send to EmailEndpoint endpoints only. Found ");
    }

    @Test
    void sendMailWithAttachments() {
        //GIVEN
        String INPUT_JSON_FILE = "messages/email_message_attachments.json";
        EmailMessage inputMessage = JsonUtils.readObjectFromClassPathResource(INPUT_JSON_FILE, EmailMessage.class);

        EmailContent emailContent = inputMessage.getBody();
        emailContent.getAttachments().forEach(attachement -> {
            try {
                attachement.setFileURI(new ClassPathResource(attachement.getFileURI().getPath()).getURI());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        //WHEN
        inputMessage = functionSend.apply(inputMessage);
        List<DeliveryInfoSendResult> delInfos = delInfoGenerator.apply(inputMessage);

        
        Assertions.assertThat(delInfos.size()).isEqualTo(1);
        DeliveryInfoSendResult delInfo = delInfos.iterator().next();
        
        Assertions.assertThat(delInfo.getStatus()).isEqualTo(DELIVERY_STATUS.SENT);
        Assertions.assertThat(delInfo.getProcessedOn()).isNotNull();
        Assertions.assertThat(delInfo.getReceivingEndpoint().getEndpointId()).isEqualTo("john.doe@objectify.sk");

        //THEN
        MimeMessage message = greenMail.getReceivedMessages()[0];
        String body = GreenMailUtil.getBody(message);

        Assertions.assertThat(body).contains("name=test1.txt", "attachment test1", "name=test2.txt", "attachment test2");
    }

    @Test
    void sendAggregateMessage() {
        //GIVEN
        String INPUT_JSON_FILE = "messages/aggregate/aggregate_output_message.json";
        EmailMessage inputMessage = JsonUtils.readObjectFromClassPathResource(INPUT_JSON_FILE, EmailMessage.class);

        //WHEN
        inputMessage = functionSend.apply(inputMessage);
        List<DeliveryInfoSendResult> delInfos = delInfoGenerator.apply(inputMessage);

        
        Assertions.assertThat(delInfos.size()).isEqualTo(1);
        DeliveryInfoSendResult delInfo = delInfos.iterator().next();
        
        Assertions.assertThat(delInfo.getStatus()).isEqualTo(DELIVERY_STATUS.SENT);
        Assertions.assertThat(delInfo.getProcessedOn()).isNotNull();
        Assertions.assertThat(delInfo.getReceivingEndpoint()).isEqualTo(inputMessage.getReceivingEndpoints().get(0));
        Assertions.assertThat(delInfo.getEventIdsAsList()).isEqualTo(inputMessage.getPreviousEventIds());

        //THEN
        MimeMessage message = greenMail.getReceivedMessages()[0];
        String msg = GreenMailUtil.getWholeMessage(message);

        EmailContent aggregated = inputMessage.getBody();

        Assertions.assertThat(msg).contains(aggregated.getSubject());
        Assertions.assertThat(msg).contains(aggregated.getText().replaceAll("\n", "\r\n"));
        aggregated.getAttachments()
                .forEach(attachment -> Assertions.assertThat(msg).contains(attachment.getName()));
    }
}
