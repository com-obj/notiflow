package com.obj.nc.functions.sink;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ActiveProfiles;

import com.icegreen.greenmail.configuration.GreenMailConfiguration;
import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.store.FolderException;
import com.icegreen.greenmail.util.GreenMailUtil;
import com.icegreen.greenmail.util.ServerSetupTest;
import com.obj.nc.BaseIntegrationTest;
import com.obj.nc.SystemPropertyActiveProfileResolver;
import com.obj.nc.domain.content.email.EmailContent;
import com.obj.nc.domain.message.EmailMessage;
import com.obj.nc.exceptions.PayloadValidationException;
import com.obj.nc.functions.processors.deliveryInfo.DeliveryInfoSendGenerator;
import com.obj.nc.functions.processors.deliveryInfo.domain.DeliveryInfo.DELIVERY_STATUS;
import com.obj.nc.functions.processors.senders.EmailSender;
import com.obj.nc.functions.processors.senders.config.EmailSenderConfigProperties;
import com.obj.nc.functions.processors.senders.dtos.DeliveryInfoSendResult;
import com.obj.nc.utils.JsonUtils;

@ActiveProfiles(value = "test", resolver = SystemPropertyActiveProfileResolver.class)
@SpringBootTest
class EmailSenderSinkTest extends BaseIntegrationTest {

//    @Autowired private JavaMailSenderImpl defaultJavaMailSender;
    @Autowired private EmailSender functionSend;
    @Autowired private DeliveryInfoSendGenerator delInfoGenerator;
    @Autowired private EmailSenderConfigProperties settings;
    
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
        String INPUT_JSON_FILE = "messages/email_message.json";
        EmailMessage message = JsonUtils.readObjectFromClassPathResource(INPUT_JSON_FILE, EmailMessage.class);
        UUID originalProcessingId = message.getProcessingInfo().getProcessingId();

        //WHEN
        message = functionSend.apply(message);
        List<DeliveryInfoSendResult> delInfos = delInfoGenerator.apply(message);

        
        Assertions.assertThat(delInfos.size()).isEqualTo(1);
        DeliveryInfoSendResult delInfo = delInfos.iterator().next();
        Assertions.assertThat(delInfo.getStatus()).isEqualTo(DELIVERY_STATUS.SENT);
        Assertions.assertThat(delInfo.getProcessedOn()).isNotNull();
        Assertions.assertThat(delInfo.getRecievingEndpoint()).isEqualTo(message.getRecievingEndpoints().get(0));
        Assertions.assertThat(delInfo.getEventIdsAsList()).isEqualTo(message.getHeader().getEventIds());

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
        Assertions.assertThat(delInfo.getRecievingEndpoint().getEndpointId()).isEqualTo("john.doe@objectify.sk");

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
        Assertions.assertThat(delInfo.getRecievingEndpoint()).isEqualTo(inputMessage.getRecievingEndpoints().get(0));
        Assertions.assertThat(delInfo.getEventIdsAsList()).isEqualTo(inputMessage.getHeader().getEventIds());

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
