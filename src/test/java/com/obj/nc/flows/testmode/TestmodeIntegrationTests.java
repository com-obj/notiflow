package com.obj.nc.flows.testmode;

import static com.obj.nc.flows.emailFormattingAndSending.EmailProcessingFlowConfig.EMAIL_FORMAT_AND_SEND_FLOW_INPUT_CHANNEL_ID;
import static com.obj.nc.flows.smsFormattingAndSending.SmsProcessingFlowConfig.SMS_PROCESSING_FLOW_INPUT_CHANNEL_ID;
import static com.obj.nc.flows.testmode.email.config.TestModeEmailsFlowConfig.TEST_MODE_GREEN_MAIL_SOURCE_BEAN_NAME;
import static com.obj.nc.flows.testmode.sms.config.TestModeSmsFlowConfig.TEST_MODE_SMS_SOURCE_BEAN_NAME;
import static com.obj.nc.flows.testmode.sms.config.TestModeSmsFlowConfig.TEST_MODE_SMS_SOURCE_TRIGGER_BEAN_NAME;
import static org.awaitility.Awaitility.await;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.config.EnableIntegrationManagement;
import org.springframework.integration.config.EnableMessageHistory;
import org.springframework.integration.core.MessageSource;
import org.springframework.integration.test.context.MockIntegrationContext;
import org.springframework.integration.test.context.SpringIntegrationTest;
import org.springframework.integration.test.util.OnlyOnceTrigger;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.scheduling.Trigger;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;

import com.icegreen.greenmail.configuration.GreenMailConfiguration;
import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.store.FolderException;
import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.GreenMailUtil;
import com.icegreen.greenmail.util.ServerSetupTest;
import com.obj.nc.testUtils.BaseIntegrationTest;
import com.obj.nc.testUtils.SystemPropertyActiveProfileResolver;
import com.obj.nc.domain.content.email.EmailContent;
import com.obj.nc.domain.message.EmailMessage;
import com.obj.nc.domain.message.EmailMessageTemplated;
import com.obj.nc.domain.message.SmsMessage;
import com.obj.nc.domain.message.SmsMessageTemplated;
import com.obj.nc.flows.testmode.email.config.TestModeEmailsBeansConfig;
import com.obj.nc.flows.testmode.email.config.TestModeEmailsFlowConfig;
import com.obj.nc.flows.testmode.email.config.TestModeGreenMailProperties;
import com.obj.nc.flows.testmode.email.functions.sources.GreenMailReceiverSourceSupplier;
import com.obj.nc.flows.testmode.sms.funcitons.sources.InMemorySmsSourceSupplier;
import com.obj.nc.functions.processors.senders.EmailSender;
import com.obj.nc.utils.JsonUtils;

@ActiveProfiles(value = { "test"}, resolver = SystemPropertyActiveProfileResolver.class)
@SpringIntegrationTest(noAutoStartup = {TEST_MODE_GREEN_MAIL_SOURCE_BEAN_NAME, TEST_MODE_SMS_SOURCE_BEAN_NAME })
@SpringBootTest(properties = {
		"nc.flows.test-mode.enabled=true", 
		"nc.flows.test-mode.recipients=cuzy@objectify.sk"})
@DirtiesContext(classMode = ClassMode.AFTER_CLASS) //need to dispose @Qualifier(TestModeEmailsBeansConfig.TEST_MODE_GREEN_MAIL_BEAN_NAME) testModeEmailsReciver
@Tag("test-mode")
public class TestmodeIntegrationTests extends BaseIntegrationTest {
	
	@Qualifier(TestModeEmailsBeansConfig.TEST_MODE_GREEN_MAIL_BEAN_NAME)
	@Autowired private GreenMail testModeEmailsReciver;
    
    @Qualifier(EMAIL_FORMAT_AND_SEND_FLOW_INPUT_CHANNEL_ID)
    @Autowired private MessageChannel emailProcessingInputChannel;
    @Qualifier(SMS_PROCESSING_FLOW_INPUT_CHANNEL_ID)
    @Autowired private MessageChannel smsProcessingInputChannel;
	
	@Autowired private EmailSender emailSender;
	@Autowired private GreenMailReceiverSourceSupplier greenMailReceiverSourceSupplier;
    @Autowired private InMemorySmsSourceSupplier smsSourceSupplier;
	@Autowired private TestModeGreenMailProperties gmProps;
	@Autowired private TestModeProperties props;
	@Autowired private MockIntegrationContext mockIntegrationContext;
	@Autowired private JavaMailSenderImpl javaMailSender;
	
    @RegisterExtension
    protected static GreenMailExtension greenMail = new GreenMailExtension(ServerSetupTest.SMTP)
      	.withConfiguration(
      			GreenMailConfiguration.aConfig()
      			.withUser("no-reply@objectify.sk", "xxx"))
      	.withPerMethodLifecycle(true);

    @BeforeEach
    void setUp() throws FolderException {
    	testModeEmailsReciver.purgeEmailFromAllMailboxes();
    	greenMail.purgeEmailFromAllMailboxes();
    	smsSourceSupplier.purgeAllReceivedMessages();
    }

    @Test
    void testTestmode() throws MessagingException {
    	//normaly, we would send all test mail to standrdTestGMServer. When testMode profile is activated, aditional testModeEmailsReciver
    	//is created which is a different instannce. In this mode testModeEmailsReciver will catch all emails normaly send to standardTestGMServer
    	//and thus in production will catch all emails send to standard SMTP server configured
    	//PRE-CONDITION
    	Assertions.assertThat(greenMail).isNotEqualTo(testModeEmailsReciver);
    	Assertions.assertThat(greenMail.getSmtp().getPort()).isNotEqualTo(testModeEmailsReciver.getSmtp().getPort());

    	//PRE-CONDITION
    	//Any injected JavaMailSenderImpl has to be configured to send email to testModeEmailReviver. Not to standard SMTP
    	Assertions.assertThat(javaMailSender.getHost()).isEqualTo("localhost");
    	Assertions.assertThat(javaMailSender.getPort()).isEqualTo(gmProps.getSmtpPort());
    	
    	
        // GIVEN
    	EmailMessage message1 = JsonUtils.readObjectFromClassPathResource("messages/testmode/aggregate_input_message1.json", EmailMessage.class);
    	EmailMessage message2 = JsonUtils.readObjectFromClassPathResource("messages/testmode/aggregate_input_message2.json", EmailMessage.class);
    	EmailMessage message3 = JsonUtils.readObjectFromClassPathResource("messages/testmode/aggregate_input_message3.json", EmailMessage.class);
        
        //WHEN
        emailSender.apply(message1);
        emailSender.apply(message2);
        emailSender.apply(message3);

        testModeEmailsReciver.waitForIncomingEmail(3);

        //THEN MeSSAGES RECIEVED to TESTMODE GREENMAIL
        MimeMessage[] inputMimeMessages = testModeEmailsReciver.getReceivedMessages();
        Assertions.assertThat(inputMimeMessages.length).isEqualTo(3);

        List<EmailMessage> messages = greenMailReceiverSourceSupplier.get();
//        List<Message> messages = messagesWrapped.getMessages();

        // WHEN Simulate further aggregation processing
        MessageSource<?> messageSource = () -> new GenericMessage<>(messages);
        mockIntegrationContext.substituteMessageSourceFor(TEST_MODE_GREEN_MAIL_SOURCE_BEAN_NAME, messageSource);

        // THEN agregeted mail recieved by standardn green mail used by test and thus in producton standard SMTP server
        boolean success = greenMail.waitForIncomingEmail(30000,1);
        Assertions.assertThat( success ).isEqualTo( true );

        MimeMessage[] outputMimeMessages = greenMail.getReceivedMessages();
        Assertions.assertThat( outputMimeMessages.length ).isEqualTo(1);

        String recipient = props.getRecipients().iterator().next();
        outputMimeMessages = greenMail.getReceivedMessagesForDomain(recipient);

        MimeMessage msg = outputMimeMessages[0];
        System.out.println(GreenMailUtil.getWholeMessage(msg));
        
        
        EmailContent aggregated1 = message1.getBody();
        EmailContent aggregated2 = message2.getBody();
        EmailContent aggregated3 = message3.getBody();
        Assertions.assertThat(msg.getSubject()).isEqualTo("Notifications digest while running test mode");

        Assertions.assertThat(GreenMailUtil.getBody(msg)).contains(
        		aggregated1.getSubject(),
        		aggregated2.getSubject(),
        		aggregated3.getSubject());

        Assertions.assertThat(GreenMailUtil.getBody(msg)).contains(
        		aggregated1.getText(),
        		aggregated2.getText(),
        		aggregated3.getText());
        
        //Check tranlations
        Assertions.assertThat(GreenMailUtil.getBody(msg)).contains("Recipient","Attachments");
    }
    
    @Test
    void testSendEmailAndSmsDigestInOneEmail() {
        // GIVEN
    	EmailMessageTemplated<?> inputEmail = JsonUtils.readObjectFromClassPathResource("messages/templated/teamplate_message_en_de.json", EmailMessageTemplated.class);
    	SmsMessageTemplated<?> inputSms = JsonUtils.readObjectFromClassPathResource("messages/templated/txt_template_message_en_de.json", SmsMessageTemplated.class);
    
        //AND GIVEN RECEIVED EMAILs
        emailProcessingInputChannel.send(new GenericMessage<>(inputEmail));
        testModeEmailsReciver.waitForIncomingEmail(1);
        List<EmailMessage> receivedEmailMessages = greenMailReceiverSourceSupplier.get();
        Assertions.assertThat(receivedEmailMessages).hasSize(1);
        MessageSource<?> emailMessageSource = () -> new GenericMessage<>(receivedEmailMessages);
    
        // AND RECEIVED SMSs
        smsProcessingInputChannel.send(new GenericMessage<>(inputSms));
        await().atMost(10, TimeUnit.SECONDS).until(() -> smsSourceSupplier.getReceivedCount() >= 1);
        List<SmsMessage> receivedSmsMessages = Stream.generate(smsSourceSupplier).limit(10).filter(Objects::nonNull).collect(Collectors.toList());
        Assertions.assertThat(receivedSmsMessages).hasSize(2);
        MessageSource<?> smsMessageSource = () -> new GenericMessage<>(receivedSmsMessages);
    
        // WHEN SUBSTITUTE MESSAGE SOURCES
        mockIntegrationContext.substituteMessageSourceFor(TEST_MODE_GREEN_MAIL_SOURCE_BEAN_NAME, emailMessageSource);
        mockIntegrationContext.substituteMessageSourceFor(TEST_MODE_SMS_SOURCE_BEAN_NAME, smsMessageSource);
    
        // THEN agregeted mail recieved by standardn green mail used by test and thus in producton standard SMTP server
        boolean success = greenMail.waitForIncomingEmail(30000, 1);
        Assertions.assertThat(success).isEqualTo(true);
    
        MimeMessage[] outputMimeMessages = greenMail.getReceivedMessages();
        Assertions.assertThat(outputMimeMessages.length).isEqualTo(1);
    }
    
    @TestConfiguration
    @EnableMessageHistory
    @EnableIntegrationManagement
    public static class TestModeTestConfiguration {

        @Bean(TestModeEmailsFlowConfig.TEST_MODE_SOURCE_TRIGGER_BEAN_NAME)
        public Trigger testModeEmailSourceTrigger() {
            return new OnlyOnceTrigger();
        }
    
        @Bean(TEST_MODE_SMS_SOURCE_TRIGGER_BEAN_NAME)
        public Trigger testModeSmsSourceTrigger() {
            return new OnlyOnceTrigger();
        }

    }

}
