package com.obj.nc.testmode;

import com.icegreen.greenmail.util.GreenMailUtil;
import com.obj.nc.SystemPropertyActiveProfileResolver;
import com.obj.nc.domain.message.Message;
import com.obj.nc.functions.processors.senders.EmailSenderSinkProcessingFunction;
import com.obj.nc.testmode.functions.processors.TestModeEmailSenderProperties;
import com.obj.nc.testmode.functions.sources.GreenMailReceiverSourceSupplier;
import com.obj.nc.utils.GreenMailManager;
import com.obj.nc.utils.JsonUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.core.MessageSource;
import org.springframework.integration.test.context.MockIntegrationContext;
import org.springframework.integration.test.context.SpringIntegrationTest;
import org.springframework.integration.test.util.OnlyOnceTrigger;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.scheduling.Trigger;
import org.springframework.test.context.ActiveProfiles;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.List;

@ActiveProfiles(value = "test", resolver = SystemPropertyActiveProfileResolver.class)
@SpringBootTest
@SpringIntegrationTest(noAutoStartup = "greenMailSource")
public class TestmodeIntegrationTests {

    @Autowired
    private GreenMailManager greenMailManager;

    @Autowired
    private EmailSenderSinkProcessingFunction emailSenderSinkProcessingFunction;

    @Autowired
    private GreenMailReceiverSourceSupplier greenMailReceiverSourceSupplier;

    @Autowired
    private TestModeEmailSenderProperties properties;

    @Autowired
    private MockIntegrationContext mockIntegrationContext;

    @Test
    void testTestmode() throws MessagingException {
        // GIVEN
        Message message1 = JsonUtils.readObjectFromClassPathResource("messages/testmode/aggregate_input_message1.json", Message.class);
        emailSenderSinkProcessingFunction.apply(message1);
        Message message2 = JsonUtils.readObjectFromClassPathResource("messages/testmode/aggregate_input_message2.json", Message.class);
        emailSenderSinkProcessingFunction.apply(message2);
        Message message3 = JsonUtils.readObjectFromClassPathResource("messages/testmode/aggregate_input_message3.json", Message.class);
        emailSenderSinkProcessingFunction.apply(message3);

        greenMailManager.getGreenMail().waitForIncomingEmail(3);
        List<Message> messages = greenMailReceiverSourceSupplier.get();

        // WHEN
        MessageSource<List<Message>> messageSource = () -> new GenericMessage<>(messages);
        mockIntegrationContext.substituteMessageSourceFor("greenMailSource", messageSource);

        // THEN
        boolean success = greenMailManager.getGreenMail().waitForIncomingEmail(1);
        Assertions.assertThat( success ).isEqualTo( true );

        MimeMessage[] mimeMessages = greenMailManager.getGreenMail().getReceivedMessages();
        Assertions.assertThat( mimeMessages.length ).isEqualTo(1);

        mimeMessages = greenMailManager.getGreenMail().getReceivedMessagesForDomain(properties.getRecipient());
        Assertions.assertThat( mimeMessages.length ).isEqualTo(1);

        Assertions.assertThat(mimeMessages[0].getSubject()).contains(
                message1.getBody().getMessage().getAggregateContent().get(0).getSubject(),
                message2.getBody().getMessage().getAggregateContent().get(0).getSubject(),
                message3.getBody().getMessage().getAggregateContent().get(0).getSubject());
        Assertions.assertThat(GreenMailUtil.getBody(mimeMessages[0])).contains(
                message1.getBody().getMessage().getAggregateContent().get(0).getText(),
                message2.getBody().getMessage().getAggregateContent().get(0).getText(),
                message3.getBody().getMessage().getAggregateContent().get(0).getText());
    }

    @TestConfiguration
    public static class TestModeTestConfiguration {

        @Bean
        public Trigger sourceTrigger() {
            return new OnlyOnceTrigger();
        }

    }

}
