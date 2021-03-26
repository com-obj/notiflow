package com.obj.nc.osk.functions.sources;

import com.obj.nc.SystemPropertyActiveProfileResolver;
import com.obj.nc.domain.message.Message;
import com.obj.nc.osk.dto.OskSendSmsRequestDto;
import com.obj.nc.osk.functions.senders.OskSmsSenderConfigProperties;
import com.obj.nc.osk.services.OskSmsRestClientImpl;
import com.obj.nc.osk.services.OskSmsRestReceiver;
import com.obj.nc.utils.JsonUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import java.util.List;

@ActiveProfiles(value = "test", resolver = SystemPropertyActiveProfileResolver.class)
@RestClientTest(properties = "nc.flows.test-mode.enabled=true")
@EnableConfigurationProperties(OskSmsSenderConfigProperties.class)
@ContextConfiguration(classes = {OskSmsRestReceiverSourceSupplier.class, OskSmsRestReceiver.class, OskSmsRestClientImpl.class})
class OskSmsRestReceiverSourceSupplierTest {
    
    @Autowired
    private OskSmsRestReceiverSourceSupplier sourceSupplier;
    
    @Autowired
    private OskSmsRestReceiver receiver;
    
    @Autowired
    private OskSmsRestClientImpl restClient;

    @Test
    void testGet() {
        // GIVEN
        Message inputMessage = JsonUtils.readObjectFromClassPathResource("smsNotificationMessages/message.json", Message.class);
        OskSendSmsRequestDto oskSendSmsRequestDto = restClient.convertMessageToRequest(inputMessage);
        receiver.receive(oskSendSmsRequestDto);
        receiver.receive(oskSendSmsRequestDto);
        receiver.receive(oskSendSmsRequestDto);
        
        // WHEN
        List<OskSendSmsRequestDto> receivedRequests = sourceSupplier.get();
        
        // THEN
        Assertions.assertThat(receiver.getAllRequests()).hasSize(0);
        Assertions.assertThat(receivedRequests).hasSize(3);
        Assertions.assertThat(receivedRequests.get(0)).isEqualTo(oskSendSmsRequestDto);
        Assertions.assertThat(receivedRequests.get(1)).isEqualTo(oskSendSmsRequestDto);
        Assertions.assertThat(receivedRequests.get(2)).isEqualTo(oskSendSmsRequestDto);
    }
    
}