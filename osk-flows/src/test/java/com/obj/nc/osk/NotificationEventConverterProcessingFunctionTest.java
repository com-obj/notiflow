package com.obj.nc.osk;

import static com.obj.nc.osk.functions.NotificationEventConverterProcessingFunction.OUTAGE_INFOS_ATTR_NAME;
import static com.obj.nc.osk.functions.NotificationEventConverterProcessingFunction.OUTAGE_START_ATTR_NAME;
import static com.obj.nc.utils.JsonUtils.readObjectFromClassPathResource;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.jxpath.JXPathContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import com.obj.nc.BaseIntegrationTest;
import com.obj.nc.OskFlowsApplication;
import com.obj.nc.SystemPropertyActiveProfileResolver;
import com.obj.nc.domain.endpoints.RecievingEndpoint;
import com.obj.nc.domain.event.Event;
import com.obj.nc.domain.event.GenericEvent;
import com.obj.nc.domain.message.Content;
import com.obj.nc.osk.dto.IncidentTicketNotificationEventDto;
import com.obj.nc.osk.functions.CustEventStartEmailTemplate;
import com.obj.nc.osk.functions.NotificationEventConverterProcessingFunction;
import com.obj.nc.osk.functions.SalesEventStartEmailTemplate;
import com.obj.nc.osk.functions.model.ServiceOutageInfo;
import com.obj.nc.utils.JsonUtils;

@ActiveProfiles(value = "test", resolver = SystemPropertyActiveProfileResolver.class)
@ContextConfiguration(classes = OskFlowsApplication.class)
public class NotificationEventConverterProcessingFunctionTest extends BaseIntegrationTest {
	
	@Autowired
	private NotificationEventConverterProcessingFunction function;
    
    @Test
	@SuppressWarnings("unchecked")
    void testCustomerEvent() {
        // GIVEN
    	IncidentTicketNotificationEventDto inputEvent = readObjectFromClassPathResource("siaNotificationEvents/event-full.json", IncidentTicketNotificationEventDto.class);
    	GenericEvent event = GenericEvent.from(JsonUtils.writeObjectToJSONNode(inputEvent));

    	//WHEN
    	List<Event> result = function.apply(event);
    	
    	//THEN
    	assertThat(result.size()).isEqualTo(5);
    	
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
    	
    	List<ServiceOutageInfo> outageInfos = msgContent.getModel().getServices();
    	assertThat(outageInfos.size()).isEqualTo(2);
    	
    	context = JXPathContext.newContext(outageInfos);
    	assertThat(context.selectSingleNode(".[@productName='VPS']")).isNotNull();
     	assertThat(context.selectSingleNode(".[@productName='VPS sifrovana']")).isNotNull();
     	
    	assertThat(context.selectSingleNode(".[@customerName='Objectify, s.r.o.']")).isNotNull();
    	assertThat(context.selectSingleNode(".[@installationAddress='Mocidla 249, Myto pod Dumbierom']")).isNotNull();
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
    	assertThat(result.size()).isEqualTo(5);
    	
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
    	
    	Map<String, List<ServiceOutageInfo>> outageInfos = msgContent.getModel().getServicesPerCustomer();
    	assertThat(outageInfos.keySet().size()).isEqualTo(2);
    	assertThat(outageInfos.get("Objectify, s.r.o.").size()).isEqualTo(1);
    	assertThat(outageInfos.get("Artin, s.r.o.").size()).isEqualTo(2);
    	
    	context = JXPathContext.newContext(outageInfos.get("Artin, s.r.o."));
    	assertThat(context.selectSingleNode(".[@productName='VPS sifrovana']")).isNotNull();
     	assertThat(context.selectSingleNode(".[@productName='VPS sifrovana/nesifrovana']")).isNotNull();
     	
    	assertThat(context.selectSingleNode(".[@customerName='Artin, s.r.o.']")).isNotNull();
    	assertThat(context.selectSingleNode(".[@installationAddress='Westend tower']")).isNotNull();

    }

}

