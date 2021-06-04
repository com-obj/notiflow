package com.obj.nc.domain.notifIntent;

import org.apache.commons.lang3.NotImplementedException;
import org.springframework.data.relational.core.mapping.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.obj.nc.domain.BasePayload;
import com.obj.nc.domain.content.MessageContent;
import com.obj.nc.domain.content.email.EmailContent;
import com.obj.nc.domain.content.mailchimp.MailchimpContent;
import com.obj.nc.domain.content.sms.SimpleTextContent;
import com.obj.nc.domain.endpoints.EmailEndpoint;
import com.obj.nc.domain.endpoints.MailchimpEndpoint;
import com.obj.nc.domain.endpoints.RecievingEndpoint;
import com.obj.nc.domain.endpoints.SmsEndpoint;
import com.obj.nc.domain.message.EmailMessage;
import com.obj.nc.domain.message.MailChimpMessage;
import com.obj.nc.domain.message.Message;
import com.obj.nc.domain.message.SimpleTextMessage;
import com.obj.nc.domain.notifIntent.content.IntentContent;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.log4j.Log4j2;

@Data
@NoArgsConstructor
@ToString(callSuper = false)
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
@Table("nc_intent")
@Log4j2
/**
 * This class represents Intent to deliver *some* kind of information at *some* point in time to recipient. Use this class in case
 * that you cannot tell the details about what/when/how are stored in delivery settings of that recipient. 
 * Example:
 * 		Recipient has two endpoints: Email, SMS
 * 		His settings are that he wants to be notified using SMS in working hours and by Email outside of working hours.
 * 		In addition his settings are that he want to receive at most 3 email in a week
 * 
 * Create notification intent and let NotiFlow to decide if 
 * 		SMS should be send immediately 
 * 			OR
 * 		Email should be send immediately
 * 			OR
 * 		Email should be send later in form of email aggregate
 * @author ja
 *
 * @param <BODY_TYPE>
 */
public class NotificationIntent<BODY_TYPE extends IntentContent> extends BasePayload<BODY_TYPE> {
	
	public static final String JSON_TYPE_IDENTIFIER = "INTENT";
	
	@Override
	@JsonIgnore
	public String getPayloadTypeName() {
		return JSON_TYPE_IDENTIFIER;
	}

	public Message<?> createMessage(RecievingEndpoint endpointsForOneSubject) {		
		if (endpointsForOneSubject instanceof EmailEndpoint) {
			EmailMessage email = new EmailMessage();
			MessageContent msgContent = getBody().createMessageContent(endpointsForOneSubject);
			email.setBody((EmailContent)msgContent);
			
			return email;
		} else if (endpointsForOneSubject instanceof SmsEndpoint) {
			SimpleTextMessage sms = new SimpleTextMessage();
			MessageContent msgContent = getBody().createMessageContent(endpointsForOneSubject);
			sms.setBody((SimpleTextContent)msgContent);
			
			return sms;
		} else if (endpointsForOneSubject instanceof MailchimpEndpoint) {
			MailChimpMessage mailChimp = new MailChimpMessage();
			MessageContent msgContent = getBody().createMessageContent(endpointsForOneSubject);
			mailChimp.setBody((MailchimpContent)msgContent);
			
			return mailChimp;
		}

		throw new NotImplementedException("Add additional cases");

	}

	
}
