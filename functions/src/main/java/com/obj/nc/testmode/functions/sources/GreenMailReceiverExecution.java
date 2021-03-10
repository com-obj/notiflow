package com.obj.nc.testmode.functions.sources;

import com.icegreen.greenmail.store.FolderException;
import com.icegreen.greenmail.store.InMemoryStore;
import com.icegreen.greenmail.store.MailFolder;
import com.obj.nc.domain.endpoints.DeliveryOptions;
import com.obj.nc.domain.endpoints.EmailEndpoint;
import com.obj.nc.domain.message.Message;
import com.obj.nc.domain.message.MessageContent;
import com.obj.nc.testmode.functions.processors.TestModeEmailSenderProperties;
import com.obj.nc.utils.GreenMailManager;
import org.apache.commons.mail.util.MimeMessageParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import javax.mail.*;
import javax.mail.internet.MimeMessage;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Component
@ConditionalOnProperty(value = "testmode.enabled", havingValue = "true")
public class GreenMailReceiverExecution implements Supplier<List<Message>> {

    @Autowired
    private GreenMailManager greenMailManager;

    @Autowired
    private TestModeEmailSenderProperties properties;

    @Override
    public List<Message> get() {
        List<Message> allMessages = new ArrayList<>();

        try {
            InMemoryStore store = (InMemoryStore) greenMailManager.getGreenMail().getManagers().getImapHostManager().getStore();
            Collection<MailFolder> mailboxes = store.listMailboxes("*");

            for (MailFolder folder : mailboxes) {
                List<Message> folderMessages = folder.getMessages().stream().map(
                        message -> {
                            MimeMessage mimeMessage = message.getMimeMessage();
                            Message result = new Message();
                            try {
                                MessageContent content = result.getBody().getMessage().getContent();
                                content.setSubject(mimeMessage.getSubject());

                                MimeMessageParser parser = new MimeMessageParser(mimeMessage).parse();
                                content.setText(parser.hasHtmlContent() ? parser.getHtmlContent() : parser.getPlainContent());

                                List<MessageContent> aggregateContent = new ArrayList<>();
                                aggregateContent.add(content);
                                result.getBody().getMessage().setAggregateContent(aggregateContent);

                                EmailEndpoint emailEndpoint = new EmailEndpoint();
                                emailEndpoint.setEmail(properties.getRecipient());
                                result.getBody().setRecievingEndpoints(Collections.singletonList(emailEndpoint));

                                DeliveryOptions deliveryOptions = new DeliveryOptions();
                                deliveryOptions.setAggregationType(DeliveryOptions.AGGREGATION_TYPE.ONCE_A_DAY);
                                result.getBody().setDeliveryOptions(deliveryOptions);
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }

                            message.setFlag(Flags.Flag.DELETED, true);

                            return result;
                        }
                ).collect(Collectors.toList());

                allMessages.addAll(folderMessages);
                folder.expunge();
            }

        } catch (FolderException e) {
            throw new RuntimeException(e);
        }

        return allMessages;
    }

}
