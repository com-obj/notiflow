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

import com.obj.nc.domain.event.GenericEvent;
import com.obj.nc.flows.dataSources.DataSourceFlowsProperties;
import com.obj.nc.flows.dataSources.http.properties.HttpDataSourceProperties;
import com.obj.nc.repositories.GenericEventRepository;
import lombok.AllArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@Configuration(EventSummaryNotificationConfiguration.EVENT_SUMMARY_CONF_BEAN_NAME)
@AllArgsConstructor
public class EventSummaryNotificationConfiguration {
			
    public static final String EVENT_SUMMARY_CONF_BEAN_NAME = "EventSummaryNotificationConfBean";

    public static final String EVENT_TO_SUMMARY_MAIL_CONVERTER_BEAN_NAME = "ProcessedEventsToSummaryMailConverter";

    private final DataSourceFlowsProperties dataPullProps;
    private final ServerProperties serverProperties;
    private final EventSummaryNotificationProperties props;
    private final GenericEventRepository eventRepo;

    @Bean(EVENT_TO_SUMMARY_MAIL_CONVERTER_BEAN_NAME)
    @ConditionalOnProperty(
            value="nc.flows.event-summary-notif.default-summary-email-creation",
            havingValue = "true",
            matchIfMissing = true
    )
    public ProcessedEventsToSummaryMailConverter notifDataToEmailConverter() {
        return new ProcessedEventsToSummaryMailConverter(props, eventRepo);
    }

    @PostConstruct
    public void registerHttpPullFlow() throws Exception {
        if (props.getEventSelection() == EventSummaryNotificationProperties.SUMMARY_NOTIF_EVENT_SELECTION.NONE) {
            return;
        }

        HttpDataSourceProperties httpPullProps = new HttpDataSourceProperties();
        httpPullProps.setName(EventSummaryNotificationProperties.EVENT_SUMMARY_DS_NAME);
        httpPullProps.setUrl(
            String.format("http://%s:%d%s/events/summary-notification",
                "localhost",
                serverProperties.getPort(),
                serverProperties.getServlet().getContextPath() != null ? serverProperties.getServlet().getContextPath() : ""
            )
        );
        httpPullProps.setPojoFCCN(GenericEvent.class.getName());
        httpPullProps.setCron(props.getCron());

        dataPullProps.getHttp().add(httpPullProps);
    }

}
