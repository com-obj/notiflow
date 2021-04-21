package com.obj.nc.functions.processors.messageAggregator.aggregations;

import com.obj.nc.domain.BasePayload;
import com.obj.nc.domain.content.email.EmailContent;
import com.obj.nc.domain.content.sms.SimpleTextContent;
import com.obj.nc.domain.endpoints.EmailEndpoint;
import com.obj.nc.domain.endpoints.RecievingEndpoint;
import com.obj.nc.domain.headers.Header;
import com.obj.nc.domain.message.Message;
import com.obj.nc.flows.testmode.TestModeProperties;
import com.obj.nc.flows.testmode.email.functions.processors.TestModeDiggestMailContent;
import com.obj.nc.flows.testmode.email.functions.processors.TestModeDiggestModel;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class TestModeSingleEmailAggregationStrategy extends BasePayloadAggregationStrategy {
    
    private final TestModeProperties testModeProps;
    
    @Override
    public Object merge(List<? extends BasePayload> payloads) {
        //TODO: ked bude refactorovany header a ostatne veci tak tuto spravit novu message a neprepisovat existujucu
        Message aggregatedMessage = (Message) payloads.get(0);
        
        TestModeDiggestModel digestModel = new TestModeDiggestModel();
        payloads.stream().map(msg -> msg.getBody().getMessage())
                .filter(content -> content instanceof EmailContent).map(content -> (EmailContent) content)
                .forEach(digestModel::addEmailContent);
        
        payloads.stream().map(msg -> msg.getBody().getMessage())
                .filter(content -> content instanceof SimpleTextContent).map(content -> (SimpleTextContent) content)
                .forEach(digestModel::addSmsContent);
        
        TestModeDiggestMailContent resultMail = new TestModeDiggestMailContent();
        resultMail.setSubject("Notifications digest while running test mode");
        resultMail.setTemplateFileName("test-mode-digest.html");
        resultMail.setRequiredLocales(Arrays.asList(new Locale("en")));
        resultMail.setModel(digestModel);
        
        aggregatedMessage.getBody().setMessage(resultMail);
        
        List<RecievingEndpoint> emailEndpoints = testModeProps.getRecipients().stream().map(rec-> new EmailEndpoint(rec)).collect(Collectors.toList());
        aggregatedMessage.getBody().setRecievingEndpoints(emailEndpoints);
        
        aggregatedMessage.getHeader().setAttributeValue(Header.SUPRESS_GENERATE_PROC_INFO_PARAM_NAME, true);
        
        return aggregatedMessage;
    }
    
}
