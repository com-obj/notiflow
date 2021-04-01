package com.obj.nc.osk.services;

import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import com.obj.nc.SystemPropertyActiveProfileResolver;
import com.obj.nc.domain.message.Message;
import com.obj.nc.osk.functions.processors.sms.OskSmsRestReceiver;
import com.obj.nc.osk.functions.processors.sms.OskSmsSenderRestImpl;
import com.obj.nc.osk.functions.processors.sms.dtos.OskSendSmsRequestDto;
import com.obj.nc.osk.functions.processors.sms.dtos.OskSendSmsResponseDto;
import com.obj.nc.utils.JsonUtils;

@ActiveProfiles(value = "test", resolver = SystemPropertyActiveProfileResolver.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext
class OskSmsRestReceiverTest {
    
    @Autowired
    private OskSmsSenderRestImpl restClient;
    
    @Autowired
    private OskSmsRestReceiver receiver;
    
    @Test
    void testWaitForIncomingRequests() {
        // GIVEN
        Message inputMessage = JsonUtils.readObjectFromClassPathResource("smsNotificationMessages/message.json", Message.class);
        OskSendSmsRequestDto oskSendSmsRequestDto = restClient.convertMessageToRequest(inputMessage);
        restClient.sendRequest(oskSendSmsRequestDto);
        restClient.sendRequest(oskSendSmsRequestDto);
        restClient.sendRequest(oskSendSmsRequestDto);
        
        // WHEN
        receiver.waitForIncomingRequests(10000L, 3);
        
        // THEN
        List<OskSendSmsRequestDto> requests = receiver.getAllRequestsAndReset();
        Assertions.assertThat(requests).hasSize(3);
        Assertions.assertThat(requests.get(0)).isEqualTo(oskSendSmsRequestDto);
        Assertions.assertThat(requests.get(1)).isEqualTo(oskSendSmsRequestDto);
        Assertions.assertThat(requests.get(2)).isEqualTo(oskSendSmsRequestDto);
    }
    
    @Test
    void testReceive() {
        // GIVEN
        Message inputMessage = JsonUtils.readObjectFromClassPathResource("smsNotificationMessages/message.json", Message.class);
        OskSendSmsRequestDto oskSendSmsRequestDto = restClient.convertMessageToRequest(inputMessage);
        
        // WHEN
        OskSendSmsResponseDto received = receiver.receive(oskSendSmsRequestDto);
        
        // then
        Assertions.assertThat(received.getResourceReference().getResourceURL())
                .contains(oskSendSmsRequestDto.getAddress().get(0))
                .contains(oskSendSmsRequestDto.getSenderAddress())
                .contains("SUCCESS");
    }
    
}