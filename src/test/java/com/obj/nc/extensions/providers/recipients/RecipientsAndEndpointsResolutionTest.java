package com.obj.nc.extensions.providers.recipients;

import static com.obj.nc.flows.inputEventRouting.config.InputEventRoutingFlowConfig.GENERIC_EVENT_CHANNEL_ADAPTER_BEAN_NAME;

import java.util.List;
import java.util.UUID;

import com.obj.nc.domain.endpoints.EmailEndpoint;
import com.obj.nc.domain.endpoints.ReceivingEndpoint;
import com.obj.nc.domain.endpoints.SmsEndpoint;
import com.obj.nc.domain.recipients.Group;
import com.obj.nc.domain.recipients.Person;
import com.obj.nc.domain.recipients.Recipient;
import com.obj.nc.testUtils.BaseIntegrationTest;
import com.obj.nc.testUtils.SystemPropertyActiveProfileResolver;

import org.junit.Ignore;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.integration.test.context.SpringIntegrationTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles(value = "test", resolver = SystemPropertyActiveProfileResolver.class)
@SpringBootTest(properties = {
    "nc.contacts-store.jsonStorePathAndFileName=src/test/resources/contact-store/contact-store.json", 
})
@SpringIntegrationTest(noAutoStartup = GENERIC_EVENT_CHANNEL_ADAPTER_BEAN_NAME)
public class RecipientsAndEndpointsResolutionTest extends BaseIntegrationTest {

    @Autowired
    protected ContactsStoreConfigProperties config;
    @Autowired 
    protected ContactsJsonStoreProvider contactStore;

    @Test
    public void testFindEndpoints() {
        List<ReceivingEndpoint> endpoints = contactStore.findEndpoints(UUID.fromString("baf25cbe-2975-4666-adda-c8ea01dc909d"));

        Assertions.assertEquals(endpoints.size(),1);
        Assertions.assertEquals(endpoints.iterator().next().getEndpointId(),"john.doe@objectify.sk");
        Assertions.assertEquals(endpoints.iterator().next().getEndpointType(),EmailEndpoint.JSON_TYPE_IDENTIFIER);
    }

    @Test
    public void testFindById() {
        List<Recipient> recipients = contactStore.findRecipients(
            UUID.fromString("baf25cbe-2975-4666-adda-c8ea01dc909d"),
            UUID.fromString("ee61d11c-f4e4-403d-befb-ad9a6766c106")
            );

        Assertions.assertEquals(recipients.size(),2);
        Assertions.assertEquals(recipients.iterator().next().getName(),"John Doe");
    }

    @Test
    public void testFindByName() {
        List<Recipient> recipients = contactStore.findRecipientsByName(
            "John Doe", "John Dudly"
        );

        Assertions.assertEquals(recipients.size(),2);
        Assertions.assertEquals(recipients.iterator().next().getName(),"John Doe");
    }

    @Test
    @Disabled("only one time initialization of contact store")
    public void createTestDataInStore() {
        Person p1 = Person.builder()
            .name("John Doe")
            .id(UUID.fromString("baf25cbe-2975-4666-adda-c8ea01dc909d"))
            .receivingEndpoint(EmailEndpoint.builder().email("john.doe@objectify.sk").build())
            .build();

        Person p2 = Person.builder()
            .name("John Dudly")
            .id(UUID.fromString("ee61d11c-f4e4-403d-befb-ad9a6766c106"))
            .receivingEndpoint(EmailEndpoint.builder().email("john.dudly@objectify.sk").build())
            .build();            

        Person p3 = Person.builder()
            .name("Invalid")
            .id(UUID.fromString("6b31e60a-7c2d-44ff-9938-34554102b48a"))
            .receivingEndpoint(EmailEndpoint.builder().email("invalid mail").build())
            .build();  

        Person p4 = Person.builder()
            .name("Jonson and Johnson")
            .id(UUID.fromString("2f33ed55-bca0-493e-b71d-0cf13b29b629"))
            .build();         
            
        Person p5 = Person.builder()
            .name("Phone")
            .id(UUID.fromString("081dd763-d6f9-4a89-81ff-f7ffb1fa088b"))
            .receivingEndpoint(SmsEndpoint.builder().phone("+421905000111").build())                                    
            .build();               

        Group objGroup = Group.builder()
            .name("Objectify")
            .id(UUID.fromString("ce48aab7-7160-4b1d-b22c-0bd1bb8bbce2"))
            .receivingEndpoint(EmailEndpoint.builder().email("all@objectify.sk").build())

            .member(
                Group.builder()
                    .name("Marketing")
                    .id(UUID.fromString("89372129-b5c9-42d0-a354-1a835dec86a9"))                            
                    .member(Person.builder()
                        .name("David S")
                        .receivingEndpoint(EmailEndpoint.builder().email("stolarik@objectify.sk").build())
                        .receivingEndpoint(SmsEndpoint.builder().phone("+421905012312").build())
                        .build())
                    .build()
            ).member(
                Group.builder()
                .name("FE Department")
                .id(UUID.fromString("bf05e1c1-e012-4723-ad12-a53e95bcdc3b"))                            
                .member(Person.builder()
                    .name("David L")
                    .receivingEndpoint(EmailEndpoint.builder().email("lazar@objectify.sk").build())
                    .receivingEndpoint(SmsEndpoint.builder().phone("+421911111111").build())
                    .build()
                )
                .build()
            ).member(Person.builder()
                .name("Jan")
                .receivingEndpoint(EmailEndpoint.builder().email("cuzy@objectify.sk").build())
                .receivingEndpoint(SmsEndpoint.builder().phone("+4219131554").build())
                .build()
            ).build();

        contactStore.addRecipients(p1,p2,p3,p4,p5,objGroup);
        contactStore.flushRepository();
    }

    
}
