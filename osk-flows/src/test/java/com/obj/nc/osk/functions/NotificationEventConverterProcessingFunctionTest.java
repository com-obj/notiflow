package com.obj.nc.osk.functions;

import static com.obj.nc.utils.JsonUtils.readObjectFromClassPathResource;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.commons.jxpath.JXPathContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import com.icegreen.greenmail.store.FolderException;
import com.obj.nc.BaseIntegrationTest;
import com.obj.nc.SystemPropertyActiveProfileResolver;
import com.obj.nc.domain.endpoints.RecievingEndpoint;
import com.obj.nc.domain.event.Event;
import com.obj.nc.domain.event.GenericEvent;
import com.obj.nc.osk.dto.IncidentTicketNotificationEventDto;
import com.obj.nc.osk.functions.content.CustEventStartEmailTemplate;
import com.obj.nc.osk.functions.content.SalesAgentsEventStartEmailTemplate;
import com.obj.nc.osk.functions.content.SalesEventStartEmailTemplate;
import com.obj.nc.osk.functions.model.CustomerInfo;
import com.obj.nc.osk.functions.model.ServiceOutageInfo;
import com.obj.nc.utils.JsonUtils;

@ActiveProfiles(value = { "test"}, resolver = SystemPropertyActiveProfileResolver.class)
@SpringBootTest
public class NotificationEventConverterProcessingFunctionTest extends BaseIntegrationTest {
	
	@Autowired
	private NotifEventConverterProcessingFunction function;
	
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    @BeforeEach
    void cleanGreenMailMailBoxes() throws FolderException, IOException {
    	greenMail.purgeEmailFromAllMailboxes();
    	
        jdbcTemplate.batchUpdate("delete from nc_processing_info");
        jdbcTemplate.batchUpdate("delete from nc_endpoint_processing");
        jdbcTemplate.batchUpdate("delete from nc_endpoint");        
        jdbcTemplate.batchUpdate("delete from nc_input");       
    }
    
    @Test
	@SuppressWarnings("unchecked")
    void testCustomerEvent() {
        // GIVEN
    	IncidentTicketNotificationEventDto inputEvent = readObjectFromClassPathResource("siaNotificationEvents/event-full.json", IncidentTicketNotificationEventDto.class);
    	GenericEvent event = GenericEvent.from(JsonUtils.writeObjectToJSONNode(inputEvent));

    	//WHEN
    	List<Event> result = function.apply(event);
    	
    	//THEN
    	assertThat(result.size()).isEqualTo(6); //3xcustomers, 2xsales, 1xsales agent 
    	
    	//THEN check events for customer
    	JXPathContext context = JXPathContext.newContext(result);
		List<Event> eventsForCuzy = context.selectNodes("//recievingEndpoints[@endpointId='cuzy@objectify.sk']/../..");
    	
    	assertThat(eventsForCuzy.size()).isEqualTo(1);
    	
    	context = JXPathContext.newContext(eventsForCuzy);
    	List<RecievingEndpoint> endpoints = context.selectNodes("//recievingEndpoints");
    	assertThat(endpoints.size()).isEqualTo(2);
    	
    	assertThat(endpoints.iterator().next().getRecipient().getName()).isEqualTo("Jan Cuzy");
    	
    	Event eventForCuzy = eventsForCuzy.iterator().next();
    	CustEventStartEmailTemplate msgContent = eventForCuzy.getContentTyped();
    	assertThat(msgContent.getModel().getTimeStart()).isNotNull();
    	
    	//THEN check outage infos
    	List<ServiceOutageInfo> outageInfos = msgContent.getModel().getServices();
    	assertThat(outageInfos.size()).isEqualTo(2);

    	context = JXPathContext.newContext(outageInfos);
    	assertThat(context.selectSingleNode(".[@productName='VPS']")).isNotNull();
     	assertThat(context.selectSingleNode(".[@productName='VPS sifrovana']")).isNotNull();
     	
    	assertThat(context.selectSingleNode(".[@customerName='Objectify, s.r.o.']")).isNotNull();
    	assertThat(context.selectSingleNode(".[@installationAddress='Mocidla 249, Myto pod Dumbierom']")).isNotNull();
    	assertThat(context.selectSingleNode(".[@customerAddress='Martinengova 4881/36 811 02 Bratislava']")).isNotNull();
    }
    
    @Test
	@SuppressWarnings("unchecked")
    void testSalesEvent() {
        // GIVEN
    	IncidentTicketNotificationEventDto inputEvent = readObjectFromClassPathResource("siaNotificationEvents/event-full.json", IncidentTicketNotificationEventDto.class);
    	GenericEvent event = GenericEvent.from(JsonUtils.writeObjectToJSONNode(inputEvent));

    	//WHEN
    	List<Event> result = function.apply(event);
    	
    	//THEN
    	assertThat(result.size()).isEqualTo(6); //3xcustomers, 2xsales, 1xsales agent 
    	
    	JXPathContext context = JXPathContext.newContext(result);
		List<Event> eventsForSlavkovsky = context.selectNodes("//recievingEndpoints[@endpointId='slavkovsky@orange.sk']/../..");
    	assertThat(eventsForSlavkovsky.size()).isEqualTo(1);
    	    	
    	Event eventForHahn = eventsForSlavkovsky.iterator().next();
    	context = JXPathContext.newContext(eventForHahn);
    	List<RecievingEndpoint> endpoints = context.selectNodes("//recievingEndpoints");
    	assertThat(endpoints.size()).isEqualTo(1);
    	
    	assertThat(endpoints.iterator().next().getRecipient().getName()).isEqualTo("Adrian Slavkovsky");
    	
    	SalesEventStartEmailTemplate msgContent = eventForHahn.getContentTyped();
    	assertThat(msgContent.getModel().getTimeStart()).isNotNull();
    	
    	Map<CustomerInfo, List<ServiceOutageInfo>> outageInfos = msgContent.getModel().getServicesPerCustomer();
    	assertThat(outageInfos.keySet().size()).isEqualTo(2);
    	assertThat(outageInfos.get(new CustomerInfo("Objectify, s.r.o.")).size()).isEqualTo(1);
    	assertThat(outageInfos.get(new CustomerInfo("Artin, s.r.o.")).size()).isEqualTo(2);
    	
    	context = JXPathContext.newContext(outageInfos.get(new CustomerInfo("Artin, s.r.o.")));
    	assertThat(context.selectSingleNode(".[@productName='VPS sifrovana']")).isNotNull();
     	assertThat(context.selectSingleNode(".[@productName='VPS sifrovana/nesifrovana']")).isNotNull();
     	
    	assertThat(context.selectSingleNode(".[@customerName='Artin, s.r.o.']")).isNotNull();
    	assertThat(context.selectSingleNode(".[@installationAddress='Westend tower']")).isNotNull();
    	assertThat(context.selectSingleNode(".[@customerAddress='Dubravska cesta 2 841 04 Bratislava']")).isNotNull();

    }
    
    @Test
	@SuppressWarnings("unchecked")
    void testSalesAgentEvent() {
        // GIVEN
    	IncidentTicketNotificationEventDto inputEvent = readObjectFromClassPathResource("siaNotificationEvents/event-full.json", IncidentTicketNotificationEventDto.class);
    	GenericEvent event = GenericEvent.from(JsonUtils.writeObjectToJSONNode(inputEvent));

    	//WHEN
    	List<Event> result = function.apply(event);
    	
    	//THEN
    	assertThat(result.size()).isEqualTo(6); //3xcustomers, 2xsales, 1xsales agent 
    	
    	//THEN check events for agent
    	JXPathContext context = JXPathContext.newContext(result);
		List<Event> eventsForAgent = context.selectNodes("//recievingEndpoints[@endpointId='sales@objectify.sk']/../..");
    	
    	assertThat(eventsForAgent.size()).isEqualTo(1);
    	
    	context = JXPathContext.newContext(eventsForAgent);
    	List<RecievingEndpoint> endpoints = context.selectNodes("//recievingEndpoints");
    	assertThat(endpoints.size()).isEqualTo(1);
    	
    	assertThat(endpoints.iterator().next().getRecipient()).isNull(); //Pre sales agentov nemam person
    	
    	Event eventForAgent = eventsForAgent.iterator().next();
    	SalesAgentsEventStartEmailTemplate msgContent = eventForAgent.getContentTyped();
    	assertThat(msgContent.getModel().getTimeStart()).isNotNull();
    	
    	//THEN check outage infos
    	List<ServiceOutageInfo> outageInfos = msgContent.getModel().getServices();
    	assertThat(outageInfos.size()).isEqualTo(4);

    	context = JXPathContext.newContext(outageInfos);
    	assertThat(context.selectSingleNode(".[@productName='VPS']")).isNotNull();
     	assertThat(context.selectSingleNode(".[@productName='VPS sifrovana']")).isNotNull();
     	assertThat(context.selectSingleNode(".[@productName='VPS sifrovana/nesifrovana']")).isNotNull();
     	
    	assertThat(context.selectSingleNode(".[@customerName='Objectify, s.r.o.']")).isNotNull();
    	assertThat(context.selectSingleNode(".[@customerName='Artin, s.r.o.']")).isNotNull();
    	assertThat(context.selectSingleNode(".[@installationAddress='Mocidla 249, Myto pod Dumbierom']")).isNotNull();
    	assertThat(context.selectSingleNode(".[@installationAddress='Westend tower']")).isNotNull();
    	assertThat(context.selectSingleNode(".[@customerAddress='Martinengova 4881/36 811 02 Bratislava']")).isNotNull();
    	assertThat(context.selectSingleNode(".[@customerAddress='Dubravska cesta 2 841 04 Bratislava']")).isNotNull();
    	
    }

}

