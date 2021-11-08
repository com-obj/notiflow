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

package com.obj.nc.flows.teamsMessageProcessing;

import com.obj.nc.domain.message.TeamsMessage;
import com.obj.nc.flows.errorHandling.ErrorHandlingFlowConfig;
import org.springframework.integration.annotation.Gateway;
import org.springframework.integration.annotation.MessagingGateway;

import java.util.concurrent.Future;

import static com.obj.nc.flows.teamsMessageProcessing.TeamsMessageProcessingFlowConfig.TEAMS_PROCESSING_FLOW_INPUT_CHANNEL_ID;
import static com.obj.nc.flows.teamsMessageProcessing.TeamsMessageProcessingFlowConfig.TEAMS_PROCESSING_FLOW_OUTPUT_CHANNEL_ID;

@MessagingGateway(errorChannel = ErrorHandlingFlowConfig.ERROR_CHANNEL_NAME)
public interface TeamsMessageProcessingFlow {
    @Gateway(
            requestChannel = TEAMS_PROCESSING_FLOW_INPUT_CHANNEL_ID,
            replyChannel = TEAMS_PROCESSING_FLOW_OUTPUT_CHANNEL_ID
    )
    Future<TeamsMessage> sendMessage(TeamsMessage msg);
}
