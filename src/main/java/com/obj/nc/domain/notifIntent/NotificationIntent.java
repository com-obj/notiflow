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

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.obj.nc.Get;
import com.obj.nc.domain.Attachment;
import com.obj.nc.domain.BaseDynamicAttributesBean;
import com.obj.nc.domain.HasPreviousEventIds;
import com.obj.nc.domain.HasProcessingInfo;
import com.obj.nc.domain.IsNotification;
import com.obj.nc.domain.headers.HasHeader;
import com.obj.nc.domain.headers.Header;
import com.obj.nc.domain.headers.ProcessingInfo;
import com.obj.nc.domain.notifIntent.content.IntentContent;
import com.obj.nc.domain.notifIntent.content.TemplatedIntentContent;
import com.obj.nc.domain.recipients.Recipient;
import com.obj.nc.domain.stats.HasRecipients;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@ToString(callSuper = false)
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
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
public class NotificationIntent extends BaseDynamicAttributesBean implements HasHeader, HasRecipients, HasProcessingInfo, IsNotification, HasPreviousEventIds {
	
	@EqualsAndHashCode.Include
	private UUID id = UUID.randomUUID();

	private Instant timeCreated;

	protected Header header = new Header();
	
	protected IntentContent body;
	
	//TODO: purely technical staf, should be moved to header
	private List<UUID> previousEventIds = new ArrayList<>();
	
	//TODO: purely technical staf, should be moved to header
	//Can Intent have parent Intent? What is that good for?
	private List<UUID> previousIntentIds = new ArrayList<>();

	private List<Recipient> recipients = new ArrayList<Recipient>();

	public static NotificationIntent createWithStaticContent(String subject, String body, Attachment ... attachments) {
		NotificationIntent intent = new NotificationIntent();	
		intent.setBody(IntentContent.createStaticContent(subject,body));
		intent.getBody().setAttachments(Arrays.asList(attachments));
		return intent;
	}

	public static NotificationIntent createWithTemplatedContent(TemplatedIntentContent<?> templatedContent, Attachment ... attachments) {
		NotificationIntent intent = new NotificationIntent();	
		intent.setBody(templatedContent);
		intent.getBody().setAttachments(Arrays.asList(attachments));
		return intent;
	}

	public NotificationIntentPersistentState toPersistentState() {
		NotificationIntentPersistentState persistentState = new NotificationIntentPersistentState();
		persistentState.setBody(getBody());
		persistentState.setHeader(getHeader());
		persistentState.setId(getId());
		persistentState.setPreviousEventIds(previousEventIds.toArray(new UUID[0]));
		persistentState.setPreviousIntentIds(previousIntentIds.toArray(new UUID[0]));
		persistentState.setRecipientIds(getRecipientsAsId().toArray(new UUID[0]));
		persistentState.setRecipients(getRecipients());
		persistentState.setTimeCreated(getTimeCreated());
		return persistentState;	 
	}

	public List<UUID> getRecipientsAsId() {
		return recipients.stream().map(Recipient::getId).collect(Collectors.toList());
	}

	public NotificationIntent addRecipients(Recipient ... r) {
		this.recipients.addAll(Arrays.asList(r));
		return this;
	}

	public NotificationIntent addRecipientsByIds(UUID ... recipientsIds) {
		List<Recipient> recipients = Get.getContactStore().findRecipients(recipientsIds);
		this.recipients.addAll(recipients);
		return this;
	}

	public NotificationIntent addRecipientsByName(String ... names) {
		List<Recipient> recipients = Get.getContactStore().findRecipientsByName(names);
		this.recipients.addAll(recipients);
		return this;
	}

	@Override
	public void addPreviousEventId(UUID eventId) {
		previousEventIds.add(eventId);
	}
	
	public void addPreviousIntentId(UUID intentId) {
		previousIntentIds.add(intentId);
	}

	@JsonIgnore
	public ProcessingInfo getProcessingInfo() {
		return getHeader().getProcessingInfo();
	}
	
	@JsonIgnore
	public boolean isNew() {
		return timeCreated == null;
	}
		
}
