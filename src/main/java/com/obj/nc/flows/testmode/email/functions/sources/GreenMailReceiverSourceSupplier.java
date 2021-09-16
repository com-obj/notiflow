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

package com.obj.nc.flows.testmode.email.functions.sources;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.mail.Address;
import javax.mail.Flags;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.apache.commons.mail.util.MimeMessageParser;
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import com.icegreen.greenmail.store.FolderException;
import com.icegreen.greenmail.store.InMemoryStore;
import com.icegreen.greenmail.store.MailFolder;
import com.icegreen.greenmail.store.StoredMessage;
import com.icegreen.greenmail.util.GreenMail;
import com.obj.nc.domain.content.email.EmailContent;
import com.obj.nc.domain.endpoints.EmailEndpoint;
import com.obj.nc.domain.message.EmailMessage;
import com.obj.nc.exceptions.PayloadValidationException;
import com.obj.nc.flows.testmode.TestModeProperties;
import com.obj.nc.flows.testmode.email.config.TestModeEmailsBeansConfig;
import com.obj.nc.functions.processors.senders.EmailSender;
import com.obj.nc.functions.sources.SourceSupplierAdapter;
import com.obj.nc.utils.JsonUtils;

import lombok.extern.log4j.Log4j2;

@Component
@ConditionalOnProperty(value = "nc.flows.test-mode.enabled", havingValue = "true")
@Log4j2
public class GreenMailReceiverSourceSupplier extends SourceSupplierAdapter<List<EmailMessage>> {

    public static final String ORIGINAL_RECIPIENTS_EMAIL_ATTR_NAME = "ORIGINAL_RECIPIENTS_EMAIL";

	@Qualifier(TestModeEmailsBeansConfig.TEST_MODE_GREEN_MAIL_BEAN_NAME)
    @Autowired private GreenMail gm;

    @Autowired private TestModeProperties properties;

	@Override
	protected Optional<PayloadValidationException> checkPreCondition(List<EmailMessage> messages) {
		if (properties.getRecipients()==null) {
			return Optional.of(new PayloadValidationException("No recipients defined for nc.flows.test-mode.recipients. Test mode sender cannot send aggregated email"));
		}
		
        return Optional.empty();
	}

	@Override
	protected List<EmailMessage> execute() {
		log.debug("Pulling messages from Test Mode GreenMail");
		
        List<EmailMessage> allMessages = new ArrayList<>();

        try {
            InMemoryStore store = (InMemoryStore) gm.getManagers().getImapHostManager().getStore();
            Collection<MailFolder> mailboxes = store.listMailboxes("*");

            for (MailFolder folder : mailboxes) {
                List<EmailMessage> folderMessages = folder.getNonDeletedMessages().stream().map(
                        greenMailMessage -> {
                        	EmailMessage message = convertGreenMailMessageToMessage(greenMailMessage);
                            greenMailMessage.setFlag(Flags.Flag.DELETED, true);
                            return message;
                        }
                ).collect(Collectors.toList());

                allMessages.addAll(folderMessages);
                folder.expunge();
            }

        } catch (FolderException e) {
            throw new RuntimeException(e);
        }
        
        if (allMessages.size()==0) {
        	return null; //terminates further flow processing
        }

        return allMessages;
	}
	
    private EmailMessage convertGreenMailMessageToMessage(StoredMessage message) {
    	EmailMessage result = new EmailMessage();

        try {
            MimeMessage mimeMessage = message.getMimeMessage();
            EmailContent content = result.getBody();
            content.setSubject(mimeMessage.getSubject());
            
            MimeMessageParser parser = new MimeMessageParser(mimeMessage).parse();
            String originalRecipients = parser.getTo().stream().map(Address::toString).collect(Collectors.joining(","));
            String mimeMessageContent = parser.hasHtmlContent() 
            		? StringEscapeUtils.unescapeHtml4( parser.getHtmlContent() )
            		: parser.getPlainContent();
            String contentType = parser.hasHtmlContent()? MediaType.TEXT_HTML_VALUE : MediaType.TEXT_PLAIN_VALUE;
       
            content.setText(mimeMessageContent);
            content.setAttributeValue(ORIGINAL_RECIPIENTS_EMAIL_ATTR_NAME, originalRecipients);
            content.setContentType(contentType);
            //This is most likely not used,.. this mails are aggregated and bundled into separate mail, which will set the recipient from setting again
            List<EmailEndpoint> emailEndpoints = properties.getRecipients().stream().map(rec-> new EmailEndpoint(rec)).collect(Collectors.toList());
            result.setReceivingEndpoints(emailEndpoints);


//            DeliveryOptions deliveryOptions = new DeliveryOptions();
//            deliveryOptions.setAggregationType(DeliveryOptions.AGGREGATION_TYPE.ONCE_A_DAY);
//            result.setDeliveryOptions(deliveryOptions);
            
            copyHeaderValues(mimeMessage, result);
            
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return result;
    }
    
	private void copyHeaderValues(MimeMessage src, EmailMessage dest) throws MessagingException {
		Enumeration<javax.mail.Header> headers = src.getAllHeaders();
		while  (headers.hasMoreElements()) {
			javax.mail.Header header =headers.nextElement();
			
			if (header.getName().equals(EmailSender.EVENT_IDS_EMAIL_HEANDER)) {
				List<String> eventIDs = JsonUtils.readObjectFromJSONString(header.getValue(), List.class);
				List<UUID> eventUUIDs = eventIDs.stream().map(eId-> UUID.fromString(eId)).collect(Collectors.toList());
				dest.setPreviousEventIds(eventUUIDs);
			} else if (header.getName().equals(EmailSender.FLOW_ID_EMAIL_HEANDER)) {
				dest.getHeader().setFlowId(header.getValue());
			} 
			dest.getHeader().setAttributeValue(header.getName(), header.getValue());
		}
	}

}
