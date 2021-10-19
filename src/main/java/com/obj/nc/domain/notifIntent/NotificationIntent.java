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

package com.obj.nc.domain.notifIntent;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.obj.nc.domain.BasePayload;
import com.obj.nc.domain.HasPreviousIntentIds;
import com.obj.nc.domain.IsNotification;
import com.obj.nc.domain.content.MessageContent;
import com.obj.nc.domain.content.TemplateWithModelContent;
import com.obj.nc.domain.content.email.EmailContent;
import com.obj.nc.domain.content.email.TemplateWithModelEmailContent;
import com.obj.nc.domain.content.mailchimp.MailchimpContent;
import com.obj.nc.domain.content.mailchimp.TemplatedMailchimpContent;
import com.obj.nc.domain.content.push.PushContent;
import com.obj.nc.domain.content.sms.SimpleTextContent;
import com.obj.nc.domain.endpoints.EmailEndpoint;
import com.obj.nc.domain.endpoints.ReceivingEndpoint;
import com.obj.nc.domain.endpoints.SmsEndpoint;
import com.obj.nc.domain.message.EmailMessage;
import com.obj.nc.domain.message.EmailMessageTemplated;
import com.obj.nc.domain.message.MailchimpMessage;
import com.obj.nc.domain.message.Message;
import com.obj.nc.domain.message.PushMessage;
import com.obj.nc.domain.message.SmsMessage;
import com.obj.nc.domain.message.SmsMessageTemplated;
import com.obj.nc.domain.message.TemplatedMailchimpMessage;
import com.obj.nc.domain.notifIntent.content.IntentContent;
import com.obj.nc.domain.refIntegrity.Reference;
import com.obj.nc.repositories.GenericEventRepository;
import com.obj.nc.repositories.NotificationIntentRepository;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.apache.commons.lang3.NotImplementedException;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import javax.validation.constraints.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@ToString(callSuper = false)
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
@Table("nc_intent")
/**
 * This class represents Intent to deliver *some* kind of information at *some* point in time to recipient. Use this class in case
 * that you cannot tell the details about what/when/how are stored in delivery settings of that recipient. 
 * Example:
 * 		Recipient has two endpoints: Email, SMS
 * 		His settings are that he wants to be notified using SMS in working hours and by Email outside of working hours.
 * 		In addition his settings are that he want to receive at most 3 email in a week
 * 
 * Create notification intent and let Notiflow to decide if 
 * 		SMS should be send immediately 
 * 			OR
 * 		Email should be send immediately
 * 			OR
 * 		Email should be send later in form of email aggregate
 * @author ja
 *
 * @param <BODY_TYPE>
 */
public class NotificationIntent extends BasePayload<IntentContent> implements IsNotification, HasPreviousIntentIds {
	
	public static final String JSON_TYPE_IDENTIFIER = "INTENT";
	
	@NotNull
	@EqualsAndHashCode.Include
	@Transient
	@Reference(GenericEventRepository.class)
	private List<UUID> previousEventIds = new ArrayList<>();
	
	@NotNull
	@EqualsAndHashCode.Include
	@Transient
	@Reference(NotificationIntentRepository.class)
	private List<UUID> previousIntentIds = new ArrayList<>();
	
	@Override
	public void addPreviousEventId(UUID eventId) {
		previousEventIds.add(eventId);
	}
	
	public void addPreviousIntentId(UUID intentId) {
		previousIntentIds.add(intentId);
	}
	
	@JsonIgnore
	@Column("previous_event_ids")
	public void setPreviousEventIdsAsArray(UUID[] eventIds) {
		setPreviousEventIds(Arrays.asList(eventIds));
	}
	
	@JsonIgnore
	@Column("previous_intent_ids")
	public void setPreviousIntentIdsAsArray(UUID[] intentIds) {
		setPreviousIntentIds(Arrays.asList(intentIds));
	}
	
	@Column("previous_event_ids")
	public UUID[] getPreviousEventIdsAsArray() {
		return previousEventIds.toArray(new UUID[0]);
	}
	
	@Column("previous_intent_ids")
	public UUID[] getPreviousIntentIdsAsArray() {
		return previousIntentIds.toArray(new UUID[0]);
	}
	
	@Override
	@JsonIgnore
	public String getPayloadTypeName() {
		return JSON_TYPE_IDENTIFIER;
	}
	
	public static NotificationIntent createWithStaticContent(String subject, String body, ReceivingEndpoint ... endpoints) {
		NotificationIntent intent = new NotificationIntent();	
		intent.setBody(IntentContent.createStaticContent(subject,body));
		intent.addReceivingEndpoints(endpoints);
		return intent;
	}
	
	public Message<?> createMessage(ReceivingEndpoint endpointsForOneSubject) {
		MessageContent msgContent = getBody().createMessageContent(endpointsForOneSubject);

		if (msgContent instanceof EmailContent) {
			EmailMessage email = Message.newTypedMessageFrom(EmailMessage.class, this);
			email.setBody((EmailContent)msgContent);
			
			return email;
		} 
		
		if (msgContent instanceof SimpleTextContent) {
			SmsMessage sms = Message.newTypedMessageFrom(SmsMessage.class, this);
			sms.setBody((SimpleTextContent)msgContent);
			
			return sms;
		} 
		
		if (msgContent instanceof TemplateWithModelContent && endpointsForOneSubject instanceof EmailEndpoint) {
			EmailMessageTemplated<?> email = Message.newTypedMessageFrom(EmailMessageTemplated.class, this);
			email.setBody((TemplateWithModelEmailContent)msgContent);
			
			return email;
		} 
		
		if (msgContent instanceof TemplateWithModelContent && endpointsForOneSubject instanceof SmsEndpoint) {
			SmsMessageTemplated<?> sms = Message.newTypedMessageFrom(SmsMessageTemplated.class, this);
			sms.setBody((TemplateWithModelContent)msgContent);
			
			return sms;
		}
		
		if (msgContent instanceof TemplatedMailchimpContent) {
			TemplatedMailchimpMessage mailChimp = Message.newTypedMessageFrom(TemplatedMailchimpMessage.class, this);
			mailChimp.setBody((TemplatedMailchimpContent)msgContent);
			
			return mailChimp;
		}
		
		if (msgContent instanceof MailchimpContent) {
			MailchimpMessage mailChimp = Message.newTypedMessageFrom(MailchimpMessage.class, this);
			mailChimp.setBody((MailchimpContent)msgContent);
			
			return mailChimp;
		}
		
		if (msgContent instanceof PushContent) {
			PushMessage push = Message.newTypedMessageFrom(PushMessage.class, this);
			push.setBody((PushContent)msgContent);
			
			return push;
		}

		throw new NotImplementedException("Add additional cases");

	}


	
}
