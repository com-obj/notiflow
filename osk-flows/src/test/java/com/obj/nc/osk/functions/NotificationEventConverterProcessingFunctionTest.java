package com.obj.nc.osk.functions;

import static com.obj.nc.utils.JsonUtils.readObjectFromClassPathResource;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
import com.obj.nc.osk.dto.IncidentTicketOutageEndEventDto;
import com.obj.nc.osk.dto.IncidentTicketOutageStartEventDto;
import com.obj.nc.osk.functions.content.CustEventEmailTemplate;
import com.obj.nc.osk.functions.content.SalesAgentsEventEmailTemplate;
import com.obj.nc.osk.functions.content.SalesEventEmailTemplate;
import com.obj.nc.osk.functions.model.CustEventModel;
import com.obj.nc.osk.functions.model.CustomerInfo;
import com.obj.nc.osk.functions.model.ServiceOutageInfo;
import com.obj.nc.repositories.GenericEventRepository;
import com.obj.nc.utils.JsonUtils;

@ActiveProfiles(value = { "test"}, resolver = SystemPropertyActiveProfileResolver.class)
@SpringBootTest
public class NotificationEventConverterProcessingFunctionTest extends BaseIntegrationTest {
	
	@Autowired
	private StartOutageEventConverter startOutageConverter;
	
	@Autowired
	private EndOutageEventConverter endOutageConverter;
	
	@Autowired
	private GenericEventRepository eventRepo;
	
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    @BeforeEach
    void purgeNotifTables() throws FolderException, IOException {
        jdbcTemplate.batchUpdate("delete from nc_processing_info");
        jdbcTemplate.batchUpdate("delete from nc_endpoint_processing");
        jdbcTemplate.batchUpdate("delete from nc_endpoint");        
        jdbcTemplate.batchUpdate("delete from nc_input");       
    }
    
    @Test
    void testCustomerEvent() throws ParseException {
    	//WHEN OUTAGE STARTs
        GenericEvent event = readOutageStartEvent();
        event = eventRepo.save(event);
    	List<Event> result = startOutageConverter.apply(event);
    	
    	//THEN
    	assertCustomerEvents(result);

    	//WHEN OUTAGE ENDs
        event = readOutageEndEvent();
    	result = endOutageConverter.apply(event);
    	
    	//THEN
    	assertCustomerEvents(result);
    	
    	List<CustEventEmailTemplate> contents = result.stream()
    		.filter(e-> e.getBody().getMessage() instanceof CustEventEmailTemplate)
    		.map(e-> ((CustEventEmailTemplate)e.getBody().getMessage()))
    		.filter(custE-> custE.getModel().getTimeStart() != null && custE.getModel().getTimeEnd() != null)
    		.collect(Collectors.toList());
    	
    	//all contents have start and end date
    	assertThat(contents.size()).isEqualTo(3);

		CustEventModel anyModel = contents.iterator().next().getModel();
       	assertThat(anyModel.getTimeStart()).isEqualTo(JsonUtils.convertJsonDateStringToDate("2021-01-01T20:00:00.000Z"));
       	assertThat(anyModel.getTimeEnd()).isEqualTo(JsonUtils.convertJsonDateStringToDate("2021-01-01T22:00:00.000Z"));
    }

	@SuppressWarnings("unchecked")
	private void assertCustomerEvents(List<Event> result) {
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
    	CustEventEmailTemplate msgContent = eventForCuzy.getContentTyped();
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
        GenericEvent event = readOutageStartEvent();

    	//WHEN
    	List<Event> result = startOutageConverter.apply(event);
    	
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
    	
    	SalesEventEmailTemplate msgContent = eventForHahn.getContentTyped();
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
    	GenericEvent event = readOutageStartEvent();

    	//WHEN
    	List<Event> result = startOutageConverter.apply(event);
    	
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
    	SalesAgentsEventEmailTemplate msgContent = eventForAgent.getContentTyped();
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

	public static GenericEvent readOutageStartEvent() {
		IncidentTicketOutageStartEventDto inputEvent = readObjectFromClassPathResource("siaNotificationEvents/outage-start-event-full.json", IncidentTicketOutageStartEventDto.class);
    	GenericEvent event = GenericEvent.from(JsonUtils.writeObjectToJSONNode(inputEvent));
    	event.setExternalId(inputEvent.getId().toString());
		return event;
	}
	
	public static GenericEvent readOutageEndEvent() {
		IncidentTicketOutageEndEventDto inputEvent = readObjectFromClassPathResource("siaNotificationEvents/outage-end-event.json", IncidentTicketOutageEndEventDto.class);
    	GenericEvent event = GenericEvent.from(JsonUtils.writeObjectToJSONNode(inputEvent));
		return event;
	}

}

