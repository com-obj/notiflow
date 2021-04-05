package com.obj.nc.osk.functions;

import static com.obj.nc.flows.inputEventRouting.config.InputEventRoutingFlowConfig.GENERIC_EVENT_CHANNEL_ADAPTER_FLOW_ID_BEAN_NAME;
import static com.obj.nc.flows.inputEventRouting.config.InputEventRoutingFlowConfig.GENERIC_EVENT_CHANNEL_ADAPTER_PAYLOAD_TYPE_BEAN_NAME;
import static com.obj.nc.utils.JsonUtils.readObjectFromClassPathResource;
import static org.assertj.core.api.Assertions.assertThat;

import java.text.ParseException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.jxpath.JXPathContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.integration.test.context.SpringIntegrationTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;

import com.obj.nc.BaseIntegrationTest;
import com.obj.nc.SystemPropertyActiveProfileResolver;
import com.obj.nc.domain.endpoints.EmailEndpoint;
import com.obj.nc.domain.endpoints.RecievingEndpoint;
import com.obj.nc.domain.endpoints.SmsEndpoint;
import com.obj.nc.domain.event.GenericEvent;
import com.obj.nc.domain.notifIntent.NotificationIntent;
import com.obj.nc.osk.domain.incidentTicket.IncidentTicketOutageEndEventDto;
import com.obj.nc.osk.domain.incidentTicket.IncidentTicketOutageStartEventDto;
import com.obj.nc.osk.functions.content.CustEmailTemplate;
import com.obj.nc.osk.functions.content.CustSmsTemplate;
import com.obj.nc.osk.functions.content.SalesAgentsEmailTemplate;
import com.obj.nc.osk.functions.content.SalesEmailTemplate;
import com.obj.nc.osk.functions.model.CustEventModel;
import com.obj.nc.osk.functions.model.CustomerInfo;
import com.obj.nc.osk.functions.model.ServiceOutageInfo;
import com.obj.nc.repositories.GenericEventRepository;
import com.obj.nc.utils.JsonUtils;

@ActiveProfiles(value = { "test" }, resolver = SystemPropertyActiveProfileResolver.class)
@SpringBootTest
@SpringIntegrationTest(noAutoStartup = {
		GENERIC_EVENT_CHANNEL_ADAPTER_FLOW_ID_BEAN_NAME, 
		GENERIC_EVENT_CHANNEL_ADAPTER_PAYLOAD_TYPE_BEAN_NAME})
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
public class NotificationEventConverterProcessingFunctionTest extends BaseIntegrationTest {
	
	@Autowired private StartOutageEventConverter startOutageConverter;
	@Autowired private EndOutageEventConverter endOutageConverter;
	@Autowired private GenericEventRepository eventRepo;
    
    @BeforeEach
    void cleanTables() {
        purgeNotifTables();     
    }
    
    @Test
    void testCustomerEvent() throws ParseException {
    	//WHEN OUTAGE STARTs
        GenericEvent event = readOutageStartEvent();
        event = eventRepo.save(event); //we need to stave this so that issue outage End
    	List<NotificationIntent> result = startOutageConverter.apply(event);
    	
    	//THEN
    	assertCustomerNotificationIntents(result);

    	//WHEN OUTAGE ENDs
        event = readOutageEndEvent();
    	result = endOutageConverter.apply(event);
    	
    	//THEN
    	assertCustomerNotificationIntents(result);
    	
    	//THEN check if all ending emails have outage end (and start)
    	List<CustEmailTemplate> emails = extractEmails(result);
    	assertThat(emails.size()).isEqualTo(3);

		for (CustEmailTemplate email: emails) {
			CustEventModel anyModel = email.getModel();
			assertThat(anyModel.getTimeStart()).isEqualTo(JsonUtils.convertJsonDateStringToDate("2021-01-01T20:00:00.000Z"));
			assertThat(anyModel.getTimeEnd()).isEqualTo(JsonUtils.convertJsonDateStringToDate("2021-01-01T22:00:00.000Z"));
		}
       	
    	//THEN check if all ending smss have outage end (and start)
    	List<CustSmsTemplate> smss = extractSmss(result);   	
    	assertThat(smss.size()).isEqualTo(2);

    	for (CustSmsTemplate sms: smss) {
			CustEventModel smsModel = sms.getModel();
	       	assertThat(smsModel.getTimeStart()).isEqualTo(JsonUtils.convertJsonDateStringToDate("2021-01-01T20:00:00.000Z"));
	       	assertThat(smsModel.getTimeEnd()).isEqualTo(JsonUtils.convertJsonDateStringToDate("2021-01-01T22:00:00.000Z"));
    	}
    }

	private List<CustEmailTemplate> extractEmails(List<NotificationIntent> result) {
		List<CustEmailTemplate> contents = result.stream()
    		.filter(e-> e.getBody().getMessage() instanceof CustEmailTemplate)
    		.map(e-> ((CustEmailTemplate)e.getBody().getMessage()))
    		.collect(Collectors.toList());
		return contents;
	}
	
	private List<CustSmsTemplate> extractSmss(List<NotificationIntent> result) {
		List<CustSmsTemplate> contents = result.stream()
    		.filter(e-> e.getBody().getMessage() instanceof CustSmsTemplate)
    		.map(e-> ((CustSmsTemplate)e.getBody().getMessage()))
    		.collect(Collectors.toList());
		return contents;
	}

	@SuppressWarnings("unchecked")
	private void assertCustomerNotificationIntents(List<NotificationIntent> result) {
		assertThat(result.size()).isEqualTo(8); //3xcustomers email,2xcustomer sms, 2xsales, 1xsales agent 
    	
    	//THEN check events for customer
    	JXPathContext context = JXPathContext.newContext(result); //"//recievingEndpoints[@endpointId='cuzy@objectify.sk']/../.."
		List<NotificationIntent> nisForCuzy = context.selectNodes("//recipient[name='Jan Cuzy']/../../..");
    	
    	assertThat(nisForCuzy.size()).isEqualTo(2);
    	
    	context = JXPathContext.newContext(nisForCuzy);
    	List<RecievingEndpoint> endpoints = context.selectNodes("//recievingEndpoints");
    	assertThat(endpoints.size()).isEqualTo(4);
    	
    	EmailEndpoint emailEp = (EmailEndpoint)endpoints.stream().filter(ep-> (ep instanceof EmailEndpoint)).findFirst().get();
    	assertThat(emailEp.getRecipient().getName()).isEqualTo("Jan Cuzy");
    	assertThat(emailEp.getEmail()).contains("cuzy");
    	
    	SmsEndpoint smsEp = (SmsEndpoint)endpoints.stream().filter(ep-> (ep instanceof SmsEndpoint)).findFirst().get();
    	assertThat(smsEp.getRecipient().getName()).isEqualTo("Jan Cuzy");
    	assertThat(smsEp.getPhone()).contains("918186997");
    	
    	CustEmailTemplate msgContentEmail =(CustEmailTemplate)nisForCuzy.stream().map(ni->ni.getContentTyped()).filter(c -> c instanceof CustEmailTemplate).findFirst().get();
    	assertThat(msgContentEmail.getModel().getTimeStart()).isNotNull();
    	
    	//THEN check outage infos
    	List<ServiceOutageInfo> outageInfos = msgContentEmail.getModel().getServices();
    	assertThat(outageInfos.size()).isEqualTo(2);

    	context = JXPathContext.newContext(outageInfos);
    	assertThat(context.selectSingleNode(".[@productName='VPS']")).isNotNull();
     	assertThat(context.selectSingleNode(".[@productName='VPS sifrovana']")).isNotNull();
     	
    	assertThat(context.selectSingleNode(".[@customerName='Objectify, s.r.o.']")).isNotNull();
    	assertThat(context.selectSingleNode(".[@installationAddress='Mocidla 249, Myto pod Dumbierom']")).isNotNull();
    	assertThat(context.selectSingleNode(".[@customerAddress='Martinengova 4881/36 811 02 Bratislava']")).isNotNull();
    	
    	//THEN check sms content - should be the same as email
    	CustSmsTemplate msgContentSms =(CustSmsTemplate)nisForCuzy.stream().map(ni->ni.getContentTyped()).filter(c -> c instanceof CustSmsTemplate).findFirst().get();
    	
    	outageInfos = msgContentSms.getModel().getServices();
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
    	List<NotificationIntent> result = startOutageConverter.apply(event);
    	
    	//THEN
    	assertThat(result.size()).isEqualTo(8); //3xcustomers email,2xcustomer sms, 2xsales, 1xsales agent 
    	
    	JXPathContext context = JXPathContext.newContext(result);
		List<NotificationIntent> notificationIntentsForSlavkovsky = context.selectNodes("//recievingEndpoints[@endpointId='slavkovsky@orange.sk']/../..");
    	assertThat(notificationIntentsForSlavkovsky.size()).isEqualTo(1);
    	    	
    	NotificationIntent notificationIntentForHahn = notificationIntentsForSlavkovsky.iterator().next();
    	context = JXPathContext.newContext(notificationIntentForHahn);
    	List<RecievingEndpoint> endpoints = context.selectNodes("//recievingEndpoints");
    	assertThat(endpoints.size()).isEqualTo(1);
    	
    	assertThat(endpoints.iterator().next().getRecipient().getName()).isEqualTo("Adrian Slavkovsky");
    	
    	SalesEmailTemplate msgContent = notificationIntentForHahn.getContentTyped();
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
    	List<NotificationIntent> result = startOutageConverter.apply(event);
    	
    	//THEN
    	assertThat(result.size()).isEqualTo(8); //3xcustomers email,2xcustomer sms, 2xsales, 1xsales agent 
    	
    	//THEN check events for agent
    	JXPathContext context = JXPathContext.newContext(result);
		List<NotificationIntent> notificationIntentsForAgent = context.selectNodes("//recievingEndpoints[@endpointId='sales@objectify.sk']/../..");
    	
    	assertThat(notificationIntentsForAgent.size()).isEqualTo(1);
    	
    	context = JXPathContext.newContext(notificationIntentsForAgent);
    	List<RecievingEndpoint> endpoints = context.selectNodes("//recievingEndpoints");
    	assertThat(endpoints.size()).isEqualTo(1);
    	
    	assertThat(endpoints.iterator().next().getRecipient()).isNull(); //Pre sales agentov nemam person
    	
    	NotificationIntent notificationIntentForAgent = notificationIntentsForAgent.iterator().next();
    	SalesAgentsEmailTemplate msgContent = notificationIntentForAgent.getContentTyped();
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

