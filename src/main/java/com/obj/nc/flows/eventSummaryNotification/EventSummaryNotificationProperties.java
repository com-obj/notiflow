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

package com.obj.nc.flows.eventSummaryNotification;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Data
@Configuration
@ConfigurationProperties(prefix = EventSummaryNotificationProperties.CONFIG_PROPS_PREFIX)
public class EventSummaryNotificationProperties {
			
	public static final String CONFIG_PROPS_PREFIX = "nc.flows.event-summary-notif";
    public static final String EVENT_SUMMARY_DS_NAME = "NC.EventSummaryNotification";

    public enum SUMMARY_NOTIF_EVENT_SELECTION {
        NONE, ALL_EVENT, SELECTED_EVENTS
    }

    private SUMMARY_NOTIF_EVENT_SELECTION eventSelection=SUMMARY_NOTIF_EVENT_SELECTION.NONE;

    private int secondsSinceLastProcessing=60;

    private String cron="* * * * *"; // every minute

    private List<String> emailRecipients; 
    
    private String emailTemplateFileName = "event-summary.html";

    private List<AdditionalEmailRecipient> additionalEmailRecipients = new ArrayList<>();

    @Getter
    @Setter
    public static class AdditionalEmailRecipient {
        private String email;

        // nc.flows.event-summary-notif.additional-email-recipients[0].event-spel-filter-expression={'A','B','C'}.contains(#jsonPath(payloadJson.toString(), '$.@type'))
        private String eventSpelFilterExpression;
    }

}
