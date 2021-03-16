package com.obj.nc.testmode;

import com.icegreen.greenmail.store.FolderException;
import com.icegreen.greenmail.util.GreenMailUtil;
import com.obj.nc.BaseIntegrationTest;
import com.obj.nc.SystemPropertyActiveProfileResolver;
import com.obj.nc.domain.Messages;
import com.obj.nc.domain.message.Message;
import com.obj.nc.functions.processors.senders.EmailSender;
import com.obj.nc.testmode.functions.processors.TestModeEmailSenderProperties;
import com.obj.nc.testmode.functions.sources.GreenMailReceiverSourceSupplier;
import com.obj.nc.utils.GreenMailManager;
import com.obj.nc.utils.JsonUtils;
import org.apache.commons.mail.util.MimeMessageParser;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
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
import java.util.stream.Stream;

@ActiveProfiles(value = { "test", "testmode" }, resolver = SystemPropertyActiveProfileResolver.class)
@SpringIntegrationTest(noAutoStartup = "greenMailSource")
public class TestmodeIntegrationTests extends BaseIntegrationTest {

    @Autowired
    private Environment environment;

    @Autowired
    private GreenMailManager greenMailManager;

    @Autowired
    private EmailSender emailSenderSinkProcessingFunction;

    @Autowired
    private GreenMailReceiverSourceSupplier greenMailReceiverSourceSupplier;

    @Autowired
    private TestModeEmailSenderProperties properties;

    @Autowired
    private MockIntegrationContext mockIntegrationContext;
    
    @BeforeEach
    void cleanGreenMailMailBoxes() throws FolderException {
        greenMailManager.getGreenMail().purgeEmailFromAllMailboxes();
    }

    @Test
    void testTestmode() throws MessagingException {
        // GIVEN
        /*
            We need to test if original mail sender sends email to GreenMail and TestMode mail sender sends email to real smtp server,
            this is trick to differentiate between those two senders - input emails are sent from different account than output emails
        */
        Assertions.assertThat(environment.getProperty("spring.mail.username")).isNotEqualTo(properties.getUsername());

        Message message1 = JsonUtils.readObjectFromClassPathResource("messages/testmode/aggregate_input_message1.json", Message.class);
        emailSenderSinkProcessingFunction.apply(message1);
        Message message2 = JsonUtils.readObjectFromClassPathResource("messages/testmode/aggregate_input_message2.json", Message.class);
        emailSenderSinkProcessingFunction.apply(message2);
        Message message3 = JsonUtils.readObjectFromClassPathResource("messages/testmode/aggregate_input_message3.json", Message.class);
        emailSenderSinkProcessingFunction.apply(message3);

        greenMailManager.getGreenMail().waitForIncomingEmail(3);

        MimeMessage[] inputMimeMessages = greenMailManager.getGreenMail().getReceivedMessages();
        Assertions.assertThat(inputMimeMessages.length).isEqualTo(3);

        // assert message1, message2, message3 were sent from "spring.mail.username"
        Stream.of(inputMimeMessages)
                .forEach(mimeMessage -> {
                    try {
                        Assertions.assertThat(new MimeMessageParser(mimeMessage).parse().getFrom()).isEqualTo(environment.getProperty("spring.mail.username"));
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });

        Messages messagesWrapped = greenMailReceiverSourceSupplier.get();
        List<Message> messages = messagesWrapped.getMessages();

        // WHEN
        MessageSource<Messages> messageSource = () -> new GenericMessage<>(messagesWrapped);
        mockIntegrationContext.substituteMessageSourceFor("greenMailSource", messageSource);

        // THEN
        boolean success = greenMailManager.getGreenMail().waitForIncomingEmail(1);
        Assertions.assertThat( success ).isEqualTo( true );

        MimeMessage[] outputMimeMessages = greenMailManager.getGreenMail().getReceivedMessages();
        Assertions.assertThat( outputMimeMessages.length ).isEqualTo(1);

        outputMimeMessages = greenMailManager.getGreenMail().getReceivedMessagesForDomain(properties.getRecipient());
        Assertions.assertThat( outputMimeMessages.length ).isEqualTo(1);

        Stream.of(outputMimeMessages)
                .forEach(mimeMessage -> {
                    try {
                        Assertions.assertThat(new MimeMessageParser(mimeMessage).parse().getFrom()).isEqualTo(properties.getUsername());
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });

        Assertions.assertThat(outputMimeMessages[0].getSubject()).contains(
                message1.getBody().getMessage().getAggregateContent().get(0).getSubject(),
                message2.getBody().getMessage().getAggregateContent().get(0).getSubject(),
                message3.getBody().getMessage().getAggregateContent().get(0).getSubject());
        Assertions.assertThat(GreenMailUtil.getBody(outputMimeMessages[0])).contains(
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
