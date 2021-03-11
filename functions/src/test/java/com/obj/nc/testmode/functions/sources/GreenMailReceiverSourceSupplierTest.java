package com.obj.nc.testmode.functions.sources;

import com.icegreen.greenmail.store.FolderException;
import com.obj.nc.SystemPropertyActiveProfileResolver;
import com.obj.nc.domain.endpoints.DeliveryOptions;
import com.obj.nc.domain.endpoints.EmailEndpoint;
import com.obj.nc.domain.endpoints.RecievingEndpoint;
import com.obj.nc.domain.message.Message;
import com.obj.nc.functions.processors.senders.EmailSenderSinkProcessingFunction;
import com.obj.nc.testmode.functions.processors.TestModeEmailSenderProperties;
import com.obj.nc.utils.GreenMailManager;
import com.obj.nc.utils.JsonUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import javax.mail.internet.MimeMessage;
import java.util.List;

@ActiveProfiles(value = "test", resolver = SystemPropertyActiveProfileResolver.class)
@SpringBootTest
class GreenMailReceiverSourceSupplierTest {

    @Autowired
    private GreenMailManager greenMailManager;

    @Autowired
    private TestModeEmailSenderProperties properties;

    @Autowired
    private EmailSenderSinkProcessingFunction emailSenderSinkProcessingFunction;

    @Autowired
    private GreenMailReceiverSourceSupplier greenMailReceiverSourceSupplier;

    @BeforeEach
    void setUp() throws FolderException {
        greenMailManager.getGreenMail().purgeEmailFromAllMailboxes();
    }

    @Test
    void testGet() {
        // GIVEN
        Message message1 = JsonUtils.readObjectFromClassPathResource("messages/testmode/aggregate_input_message1.json", Message.class);
        emailSenderSinkProcessingFunction.apply(message1);
        Message message2 = JsonUtils.readObjectFromClassPathResource("messages/testmode/aggregate_input_message2.json", Message.class);
        emailSenderSinkProcessingFunction.apply(message2);
        Message message3 = JsonUtils.readObjectFromClassPathResource("messages/testmode/aggregate_input_message3.json", Message.class);
        emailSenderSinkProcessingFunction.apply(message3);

        boolean success = greenMailManager.getGreenMail().waitForIncomingEmail(3);
        Assertions.assertThat( success ).isEqualTo( true );
        MimeMessage[] mimeMessages = greenMailManager.getGreenMail().getReceivedMessages();
        Assertions.assertThat( mimeMessages.length ).isEqualTo(3);

        // WHEN
        List<Message> messages = greenMailReceiverSourceSupplier.get();

        // THEN
        Assertions.assertThat(messages.get(0).getBody().getMessage().getAggregateContent().get(0).getSubject()).contains(
                message1.getBody().getMessage().getAggregateContent().get(0).getSubject());
        Assertions.assertThat(messages.get(0).getBody().getMessage().getAggregateContent().get(0).getText()).contains(
                message1.getBody().getMessage().getAggregateContent().get(0).getText(),
                ((EmailEndpoint) message1.getBody().getRecievingEndpoints().get(0)).getEmail());

        Assertions.assertThat(messages.get(1).getBody().getMessage().getAggregateContent().get(0).getSubject()).contains(
                message2.getBody().getMessage().getAggregateContent().get(0).getSubject());
        Assertions.assertThat(messages.get(1).getBody().getMessage().getAggregateContent().get(0).getText()).contains(
                message2.getBody().getMessage().getAggregateContent().get(0).getText(),
                ((EmailEndpoint) message2.getBody().getRecievingEndpoints().get(0)).getEmail());

        Assertions.assertThat(messages.get(2).getBody().getMessage().getAggregateContent().get(0).getSubject()).contains(
                message3.getBody().getMessage().getAggregateContent().get(0).getSubject());
        Assertions.assertThat(messages.get(2).getBody().getMessage().getAggregateContent().get(0).getText()).contains(
                message3.getBody().getMessage().getAggregateContent().get(0).getText(),
                ((EmailEndpoint) message3.getBody().getRecievingEndpoints().get(0)).getEmail());

        Assertions.assertThat(messages.get(2).getBody().getRecievingEndpoints()).hasSize(1);
        Assertions.assertThat(((EmailEndpoint) messages.get(2).getBody().getRecievingEndpoints().get(0)).getEmail()).isEqualTo(properties.getRecipient());

        Assertions.assertThat(messages.get(2).getBody().getDeliveryOptions()).isNotNull();
        Assertions.assertThat((messages.get(2).getBody().getDeliveryOptions().getAggregationType())).isEqualTo(DeliveryOptions.AGGREGATION_TYPE.ONCE_A_DAY);
    }
}