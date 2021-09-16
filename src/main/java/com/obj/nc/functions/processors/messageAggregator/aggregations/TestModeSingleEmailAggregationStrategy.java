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

package com.obj.nc.functions.processors.messageAggregator.aggregations;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.thymeleaf.util.StringUtils;

import com.obj.nc.domain.content.MessageContent;
import com.obj.nc.domain.content.email.EmailContent;
import com.obj.nc.domain.content.email.TemplateWithModelEmailContent;
import com.obj.nc.domain.content.sms.SimpleTextContent;
import com.obj.nc.domain.endpoints.EmailEndpoint;
import com.obj.nc.domain.headers.Header;
import com.obj.nc.domain.message.EmailWithTestModeDigest;
import com.obj.nc.domain.message.Message;
import com.obj.nc.flows.testmode.TestModeProperties;
import com.obj.nc.flows.testmode.email.functions.processors.TestModeDigestModel;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TestModeSingleEmailAggregationStrategy extends BasePayloadAggregationStrategy<MessageContent> {
    
    private final TestModeProperties testModeProps;
    
    @Override
    public Object merge(List<Message<MessageContent>> payloads) {
        if (payloads.isEmpty()) return null;
            
        TestModeDigestModel digestModel = new TestModeDigestModel();
        payloads.stream()
        		.map(msg -> msg.getBody())
                .filter(content -> content instanceof EmailContent).map(content -> (EmailContent) content)
                .forEach(digestModel::addEmailContent);
        
        payloads.stream()
        		.map(msg -> msg.getBody())
                .filter(content -> content instanceof SimpleTextContent).map(content -> (SimpleTextContent) content)
                .forEach(digestModel::addSmsContent);
        
        digestModel.getSmsContents().forEach(smsContent -> smsContent.setText(StringUtils.replace(smsContent.getText(), "\n", "<br>")));
        
        TemplateWithModelEmailContent<TestModeDigestModel> resultMail = new TemplateWithModelEmailContent<TestModeDigestModel>();
        resultMail.setSubject("Notifications digest while running test mode");
        resultMail.setTemplateFileName("test-mode-digest.html");
        resultMail.setRequiredLocales(Arrays.asList(new Locale("en")));
        resultMail.setModel(digestModel);
        
        List<EmailEndpoint> emailEndpoints = testModeProps.getRecipients().stream().map(rec-> new EmailEndpoint(rec)).collect(Collectors.toList());
        
        EmailWithTestModeDigest aggregatedMessage = Message.newTypedMessageFrom(EmailWithTestModeDigest.class, payloads.toArray(new Message[0]));
        aggregatedMessage.setBody(resultMail); 
        aggregatedMessage.setReceivingEndpoints(emailEndpoints);
        
        aggregatedMessage.getHeader().setAttributeValue(Header.SUPPRESS_GENERATE_PROC_INFO_PARAM_NAME, true);
        
        return aggregatedMessage;
    }
    
}
