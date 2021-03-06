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
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.obj.nc.Get;
import com.obj.nc.domain.endpoints.ReceivingEndpoint;
import com.obj.nc.domain.headers.Header;
import com.obj.nc.domain.notifIntent.content.IntentContent;
import com.obj.nc.domain.recipients.Recipient;
import com.obj.nc.domain.refIntegrity.Reference;
import com.obj.nc.extensions.providers.recipients.ContactsProvider;
import com.obj.nc.repositories.GenericEventRepository;
import com.obj.nc.repositories.NotificationIntentRepository;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Embedded;
import org.springframework.data.relational.core.mapping.Table;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.ToString;

@Data
@NoArgsConstructor
@ToString(callSuper = false)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table("nc_intent")
public class NotificationIntentPersistentState implements Persistable<UUID> {
	
	@Id
	@EqualsAndHashCode.Include
	private UUID id;

	@CreatedDate
	private Instant timeCreated;

	@Embedded(onEmpty = Embedded.OnEmpty.USE_NULL)
	private Header header;
	
	@Column("payload_json")	
	protected IntentContent body;
	
	@Reference(GenericEventRepository.class)
	private UUID[] previousEventIds;
	
	//TODO: Can Intent have parent Intent? What is that good for?
	@Reference(NotificationIntentRepository.class)
	private UUID[] previousIntentIds;
	
	private UUID[] recipientIds;

	@JsonIgnore
	@Transient
	private List<Recipient> recipients;

	@Override
	@JsonIgnore
	@Transient
	public boolean isNew() {
		return timeCreated == null;
	}
	
	@SneakyThrows
	public NotificationIntent toIntent() {
		NotificationIntent intent = new NotificationIntent();
		intent.setBody(getBody());
		intent.setHeader(getHeader());
		intent.setId(getId());
		intent.setTimeCreated(getTimeCreated());
		intent.setPreviousEventIds(Arrays.asList(previousEventIds));
		intent.setPreviousIntentIds(Arrays.asList(previousIntentIds));

		intent.setRecipients(getRecipients());
		
		return intent;
	}

	public List<Recipient> getRecipients() {
		if (recipients == null) {
			recipients = findRecipients();
		}

		return recipients;
	}

    private List<Recipient> findRecipients() {
        ContactsProvider recipientsProvider = Get.getContactStore();

        return recipientsProvider.findRecipients(recipientIds);
    }


}
