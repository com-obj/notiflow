package com.obj.nc.functions.processors.messageAggregator.aggregations;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.thymeleaf.util.StringUtils;

import com.obj.nc.domain.content.Content;
import com.obj.nc.domain.content.email.EmailContent;
import com.obj.nc.domain.content.sms.SimpleTextContent;
import com.obj.nc.domain.endpoints.EmailEndpoint;
import com.obj.nc.domain.headers.Header;
import com.obj.nc.domain.message.Message;
import com.obj.nc.flows.testmode.TestModeProperties;
import com.obj.nc.flows.testmode.email.functions.processors.TestModeDiggestMailContent;
import com.obj.nc.flows.testmode.email.functions.processors.TestModeDiggestModel;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TestModeSingleEmailAggregationStrategy extends BasePayloadAggregationStrategy<Content> {
    
    private final TestModeProperties testModeProps;
    
    @Override
    public Object merge(List<Message<Content>> payloads) {
        if (payloads.isEmpty()) return null;
    
        //TODO: ked bude refactorovany header a ostatne veci tak tuto spravit novu message a neprepisovat existujucu
//        Message<?> aggregatedMessage = (Message<?>) payloads.get(0);
        
        TestModeDiggestModel digestModel = new TestModeDiggestModel();
        payloads.stream()
        		.map(msg -> msg.getBody())
                .filter(content -> content instanceof EmailContent).map(content -> (EmailContent) content)
                .forEach(digestModel::addEmailContent);
        
        payloads.stream()
        		.map(msg -> msg.getBody())
                .filter(content -> content instanceof SimpleTextContent).map(content -> (SimpleTextContent) content)
                .forEach(digestModel::addSmsContent);
        
        digestModel.getSmsContents().forEach(smsContent -> smsContent.setText(StringUtils.replace(smsContent.getText(), "\n", "<br>")));
        
        TestModeDiggestMailContent resultMail = new TestModeDiggestMailContent();
        resultMail.setSubject("Notifications digest while running test mode");
        resultMail.setTemplateFileName("test-mode-digest.html");
        resultMail.setRequiredLocales(Arrays.asList(new Locale("en")));
        resultMail.setModel(digestModel);
        
        List<EmailEndpoint> emailEndpoints = testModeProps.getRecipients().stream().map(rec-> new EmailEndpoint(rec)).collect(Collectors.toList());
        resultMail.setRecievingEndpoints(emailEndpoints);
        
        Message<TestModeDiggestMailContent> aggregatedMessage = new Message<>();
        aggregatedMessage.setBody(resultMail);        
        
        aggregatedMessage.getHeader().setAttributeValue(Header.SUPRESS_GENERATE_PROC_INFO_PARAM_NAME, true);
        
        return aggregatedMessage;
    }
    
}
