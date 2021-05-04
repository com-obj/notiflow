package com.obj.nc.chaosmonkey;

import com.obj.nc.SystemPropertyActiveProfileResolver;
import com.obj.nc.config.InjectorConfiguration;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;


@ActiveProfiles(value = {"test", "chaos-monkey"}, resolver = SystemPropertyActiveProfileResolver.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "chaos.monkey.enabled=true",
        "management.endpoint.chaosmonkey.enabled=true",
        "management.endpoints.web.exposure.include=*"
})
@Import(InjectorConfiguration.class)
public class ChaosMonkeyEndpointsTest {
    
    @LocalServerPort
    private int port;
    
    @Test
    void testChaosMonkeyEnabled() throws IOException {
        HttpUriRequest request = new HttpGet( "http://localhost:" + port + "/actuator/chaosmonkey/status" );
        // When
        HttpResponse httpResponse = HttpClientBuilder.create().build().execute( request );
    
        // Then
        assertThat(httpResponse.getStatusLine().getStatusCode(), equalTo(HttpStatus.OK.value()));
        String contentString = EntityUtils.toString(httpResponse.getEntity());
        assertThat(contentString, containsString("Ready to be evil!"));
        
    }
    
}
