package com.obj.nc.osk.functions.sources;

import com.obj.nc.SystemPropertyActiveProfileResolver;
import com.obj.nc.osk.dto.OskSendSmsRequestDto;
import com.obj.nc.osk.services.OskSmsRestReceiver;
import com.obj.nc.utils.JsonUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles(value = "test", resolver = SystemPropertyActiveProfileResolver.class)
@SpringBootTest
class OskSmsRestReceiverSourceSupplierTest {
    
    @Autowired
    private OskSmsRestReceiverSourceSupplier sourceSupplier;
    
    @Autowired
    private OskSmsRestReceiver receiver;
    
    @Test
    void testGet() {
        // GIVEN
//        JsonUtils.readObjectFromClassPathResource("")
        receiver.waitForIncomingRequests(10000L, 1);
    
        // WHEN
        List<OskSendSmsRequestDto> receivedRequests = sourceSupplier.get();
        
        // THEN
        Assertions.assertThat(receivedRequests).hasSize(1);
    }
    
}