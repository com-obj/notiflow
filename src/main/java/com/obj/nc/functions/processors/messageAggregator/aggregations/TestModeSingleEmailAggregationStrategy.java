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
