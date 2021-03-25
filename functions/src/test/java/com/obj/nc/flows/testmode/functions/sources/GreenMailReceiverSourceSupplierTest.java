package com.obj.nc.flows.testmode.functions.sources;

import java.util.List;

import javax.mail.internet.MimeMessage;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;

import com.icegreen.greenmail.store.FolderException;
import com.icegreen.greenmail.util.GreenMail;
import com.obj.nc.BaseIntegrationTest;
import com.obj.nc.SystemPropertyActiveProfileResolver;
import com.obj.nc.domain.endpoints.DeliveryOptions;
import com.obj.nc.domain.endpoints.EmailEndpoint;
import com.obj.nc.domain.message.AggregatedEmail;
import com.obj.nc.domain.message.Email;
import com.obj.nc.domain.message.Message;
import com.obj.nc.flows.testmode.TestModeBeansConfig;
import com.obj.nc.flows.testmode.TestModeProperties;
import com.obj.nc.functions.sources.GreenMailReceiverSourceSupplier;
import com.obj.nc.functions.processors.senders.EmailSender;
import com.obj.nc.utils.JsonUtils;

@ActiveProfiles(value = {"test"}, resolver = SystemPropertyActiveProfileResolver.class)
@SpringBootTest(properties = {
		"nc.flows.test-mode.enabled=true", 
		"nc.flows.test-mode.recipients=cuzy@objectify.sk",
		"nc.flows.test-mode.periodInSeconds=64000"}) //Don't poll, I'll make it by my self
@DirtiesContext(classMode = ClassMode.AFTER_CLASS) //Because of correct disposal of green mail used for test mode
class GreenMailReceiverSourceSupplierTest extends BaseIntegrationTest {

	@Qualifier(TestModeBeansConfig.TEST_MODE_GREEN_MAIL_BEAN_NAME)
    @Autowired private GreenMail testModeEmailsReciver;
	@Autowired private TestModeProperties properties;
	@Autowired private EmailSender emailSenderSinkProcessingFunction;
	@Autowired private GreenMailReceiverSourceSupplier greenMailReceiverSourceSupplier;

    @BeforeEach
    void setUp() throws FolderException {
    	testModeEmailsReciver.purgeEmailFromAllMailboxes();
    }

    @Test
    void testRecieveAndConvertMailsFromGreenMail() {
    	//normaly, we would send all test mail to standrdTestGMServer. When testMode profile is activated, aditional testModeEmailsReciver
    	//is created which is a different instannce. In this mode testModeEmailsReciver will catch all emails normaly send to standardTestGMServer
    	//and thus in production will catch all emails send to standard SMTP server configured
    	//PRE-CONDITION
    	Assertions.assertThat(greenMail).isNotEqualTo(testModeEmailsReciver);
    	Assertions.assertThat(greenMail.getSmtp().getPort()).isNotEqualTo(testModeEmailsReciver.getSmtp().getPort());
        // GIVEN
        Message message1 = JsonUtils.readObjectFromClassPathResource("messages/testmode/aggregate_input_message1.json", Message.class);
        Message message2 = JsonUtils.readObjectFromClassPathResource("messages/testmode/aggregate_input_message2.json", Message.class);
        Message message3 = JsonUtils.readObjectFromClassPathResource("messages/testmode/aggregate_input_message3.json", Message.class);

        //WHEN
        emailSenderSinkProcessingFunction.apply(message1);
        emailSenderSinkProcessingFunction.apply(message2);
        emailSenderSinkProcessingFunction.apply(message3);

        //THEN testModeEmailsReciver recieved the message, not the standard greenmail used for test. This proves it has been substituted
        boolean success = testModeEmailsReciver.waitForIncomingEmail(3);
        Assertions.assertThat( success ).isEqualTo( true );
        MimeMessage[] mimeMessages = testModeEmailsReciver.getReceivedMessages();
        Assertions.assertThat( mimeMessages.length ).isEqualTo(3);

        // WHEN
        List<Message> messages = greenMailReceiverSourceSupplier.get();

        // THEN mesages recieved from greenMailReceiverSourceSupplier and testModeEmailsReciver are as far as content the same
//        List<Message> messages = messagesWrapped.getMessages();

        Email original = messages.get(0).getContentTyped();
        AggregatedEmail aggregatedExpected = message1.getContentTyped();
        Assertions.assertThat(original.getSubject()).contains(
        		aggregatedExpected.getAggregateContent().get(0).getSubject());
        Assertions.assertThat(original.getText()).contains(
        		aggregatedExpected.getAggregateContent().get(0).getText(),
                ((EmailEndpoint) message1.getBody().getRecievingEndpoints().get(0)).getEmail());

        original = messages.get(1).getContentTyped();
        aggregatedExpected = message2.getContentTyped();
        Assertions.assertThat(original.getSubject()).contains(
        		aggregatedExpected.getAggregateContent().get(0).getSubject());
        Assertions.assertThat(original.getText()).contains(
        		aggregatedExpected.getAggregateContent().get(0).getText(),
                ((EmailEndpoint) message2.getBody().getRecievingEndpoints().get(0)).getEmail());

        original = messages.get(2).getContentTyped();
        aggregatedExpected = message3.getContentTyped();
        Assertions.assertThat(original.getSubject()).contains(
        		aggregatedExpected.getAggregateContent().get(0).getSubject());
        Assertions.assertThat(original.getText()).contains(
        		aggregatedExpected.getAggregateContent().get(0).getText(),
                ((EmailEndpoint) message3.getBody().getRecievingEndpoints().get(0)).getEmail());

        Assertions.assertThat(messages.get(2).getBody().getRecievingEndpoints()).hasSize(1);
        String recipient = properties.getRecipients().iterator().next();
        Assertions.assertThat(((EmailEndpoint) messages.get(2).getBody().getRecievingEndpoints().get(0)).getEmail()).isEqualTo(recipient);

        Assertions.assertThat(messages.get(2).getBody().getDeliveryOptions()).isNotNull();
        Assertions.assertThat((messages.get(2).getBody().getDeliveryOptions().getAggregationType())).isEqualTo(DeliveryOptions.AGGREGATION_TYPE.ONCE_A_DAY);
    }
}