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

package com.obj.nc.flows.emailFormattingAndSending;

import static com.obj.nc.flows.emailFormattingAndSending.EmailProcessingFlowConfig.EMAIL_FORMAT_AND_SEND_FLOW_INPUT_CHANNEL_ID;
import static com.obj.nc.flows.emailFormattingAndSending.EmailProcessingFlowConfig.EMAIL_SEND_FLOW_INPUT_CHANNEL_ID;
import static com.obj.nc.flows.emailFormattingAndSending.EmailProcessingFlowConfig.EMAIL_SEND_FLOW_OUTPUT_CHANNEL_ID;

import java.util.concurrent.Future;

import org.springframework.integration.annotation.Gateway;
import org.springframework.integration.annotation.MessagingGateway;

import com.obj.nc.domain.message.EmailMessage;
import com.obj.nc.domain.message.EmailMessageTemplated;
import com.obj.nc.flows.errorHandling.ErrorHandlingFlowConfig;

@MessagingGateway(errorChannel = ErrorHandlingFlowConfig.ERROR_CHANNEL_NAME)
public interface EmailProcessingFlow {

	@Gateway(requestChannel=EMAIL_SEND_FLOW_INPUT_CHANNEL_ID, replyChannel = EMAIL_SEND_FLOW_OUTPUT_CHANNEL_ID)
    Future<EmailMessage> sendEmail(EmailMessage msg);
	
	@Gateway(requestChannel= EMAIL_FORMAT_AND_SEND_FLOW_INPUT_CHANNEL_ID, replyChannel = EMAIL_SEND_FLOW_OUTPUT_CHANNEL_ID)
    Future<EmailMessage> formatAndSend(EmailMessageTemplated<?> msg);
}
