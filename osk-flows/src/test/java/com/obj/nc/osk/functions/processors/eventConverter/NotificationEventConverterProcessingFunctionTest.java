package com.obj.nc.osk.functions.processors.eventConverter;

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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import com.obj.nc.BaseIntegrationTest;
import com.obj.nc.SystemPropertyActiveProfileResolver;
import com.obj.nc.domain.content.TemplateWithModelContent;
import com.obj.nc.domain.content.email.TemplateWithModelEmailContent;
import com.obj.nc.domain.endpoints.EmailEndpoint;
import com.obj.nc.domain.endpoints.RecievingEndpoint;
import com.obj.nc.domain.endpoints.SmsEndpoint;
import com.obj.nc.domain.event.GenericEvent;
import com.obj.nc.domain.message.EmailMessageTemplated;
import com.obj.nc.domain.message.Message;
import com.obj.nc.osk.domain.IncidentTicketOutageEndEventDto;
import com.obj.nc.osk.domain.IncidentTicketOutageStartEventDto;
import com.obj.nc.osk.functions.model.CustEventModel;
import com.obj.nc.osk.functions.model.CustomerInfo;
import com.obj.nc.osk.functions.model.SalesAgentEventModel;
import com.obj.nc.osk.functions.model.SalesEventModel;
import com.obj.nc.osk.functions.model.ServiceOutageInfo;
import com.obj.nc.repositories.GenericEventRepository;
import com.obj.nc.utils.JsonUtils;

@ActiveProfiles(value = { "test" }, resolver = SystemPropertyActiveProfileResolver.class)
@SpringBootTest
public class NotificationEventConverterProcessingFunctionTest extends BaseIntegrationTest {
	
	@Autowired private StartOutageEventConverter startOutageConverter;
	@Autowired private EndOutageEventConverter endOutageConverter;
	@Autowired private GenericEventRepository eventRepo;
    
    @BeforeEach
    void cleanTables(@Autowired JdbcTemplate jdbcTemplate) {
        purgeNotifTables(jdbcTemplate);     
    }
    
    @Test
    void testCustomerEvent() throws ParseException {
    	//WHEN OUTAGE STARTs
        GenericEvent event = readOutageStartEvent();
        event = eventRepo.save(event); //we need to stave this so that issue outage End
    	List<Message<?>> result = startOutageConverter.apply(event);
    	
    	//THEN
    	assertCustomerNotificationIntents(result);

    	//WHEN OUTAGE ENDs
        event = readOutageEndEvent();
    	result = endOutageConverter.apply(event);
    	
    	//THEN
    	assertCustomerNotificationIntents(result);
    	
    	//THEN check if all ending emails have outage end (and start)
    	List<TemplateWithModelEmailContent<?>> emails = extractEmails(result);
    	assertThat(emails.size()).isEqualTo(6); //3 customer contancts (2 customers), 2 sales mails, 1 agent

		for (TemplateWithModelEmailContent<?> email: emails) {
			assertThat(email.getModel())
				.extracting("timeStart")
				.isEqualTo(JsonUtils.convertJsonDateStringToDate("2021-01-01T20:00:00.000Z"));
			assertThat(email.getModel())
				.extracting("timeEnd")
				.isEqualTo(JsonUtils.convertJsonDateStringToDate("2021-01-01T22:00:00.000Z"));
		}
       	
    	//THEN check if all ending smss have outage end (and start)
    	List<TemplateWithModelContent<?>> smss = extractSmss(result);   	
    	assertThat(smss.size()).isEqualTo(2); // two customers contacts

    	for (TemplateWithModelContent<?> sms: smss) {
	       	assertThat(sms.getModel())
	       		.extracting("timeStart")
	       		.isEqualTo(JsonUtils.convertJsonDateStringToDate("2021-01-01T20:00:00.000Z"));
	       	assertThat(sms.getModel())
	       		.extracting("timeEnd")
	       		.isEqualTo(JsonUtils.convertJsonDateStringToDate("2021-01-01T22:00:00.000Z"));
    	}
    }

	private List<TemplateWithModelEmailContent<?>> extractEmails(List<Message<?>> result) {
		List<TemplateWithModelEmailContent<?>> contents = result.stream()
    		.filter(e-> e.getBody().getClass().equals(TemplateWithModelEmailContent.class))
    		.map(e-> ((TemplateWithModelEmailContent<?>)e.getBody()))
    		.collect(Collectors.toList());
		return contents;
	}
	
	private List<TemplateWithModelContent<?>> extractSmss(List<Message<?>> result) {
		List<TemplateWithModelContent<?>> contents = result.stream()
    		.filter(e-> e.getBody().getClass().equals(TemplateWithModelContent.class))
    		.map(e-> ((TemplateWithModelContent<?>)e.getBody()))
    		.collect(Collectors.toList());
		return contents;
	}

	@SuppressWarnings("unchecked")
	private void assertCustomerNotificationIntents(List<Message<?>> result) {
		assertThat(result.size()).isEqualTo(8); //3xcustomers email,2xcustomer sms, 2xsales, 1xsales agent 
    	
    	//THEN check events for customer
    	JXPathContext context = JXPathContext.newContext(result); //"//recievingEndpoints[@endpointId='cuzy@objectify.sk']/../.."
		List<Message<?>> nisForCuzy = context.selectNodes("//recipient[name='Jan Cuzy']/../..");
    	
    	assertThat(nisForCuzy.size()).isEqualTo(2);
    	
    	context = JXPathContext.newContext(nisForCuzy);
    	List<RecievingEndpoint> endpoints = context.selectNodes("/recievingEndpoints");
    	assertThat(endpoints.size()).isEqualTo(4);
    	
    	EmailEndpoint emailEp = (EmailEndpoint)endpoints.stream().filter(ep-> (ep instanceof EmailEndpoint)).findFirst().get();
    	assertThat(emailEp.getRecipient().getName()).isEqualTo("Jan Cuzy");
    	assertThat(emailEp.getEmail()).contains("cuzy");
    	
    	SmsEndpoint smsEp = (SmsEndpoint)endpoints.stream().filter(ep-> (ep instanceof SmsEndpoint)).findFirst().get();
    	assertThat(smsEp.getRecipient().getName()).isEqualTo("Jan Cuzy");
    	assertThat(smsEp.getPhone()).contains("918186997");
    	
    	TemplateWithModelEmailContent<CustEventModel> msgContentEmail =
    			(TemplateWithModelEmailContent<CustEventModel>)nisForCuzy.stream()
    				.map(ni->ni.getBody())
    				.filter(c -> c instanceof TemplateWithModelEmailContent)
    				.findFirst()
    				.get();
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
    	TemplateWithModelContent<CustEventModel> msgContentSms =(TemplateWithModelContent<CustEventModel>)nisForCuzy.stream().map(ni->ni.getBody()).filter(c -> c instanceof TemplateWithModelContent).findFirst().get();
    	
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
    	List<Message<?>> result = startOutageConverter.apply(event);
    	
    	//THEN
    	assertThat(result.size()).isEqualTo(8); //3xcustomers email,2xcustomer sms, 2xsales, 1xsales agent 
    	
    	JXPathContext context = JXPathContext.newContext(result);
		List<Message<?>> notificationIntentsForSlavkovsky = context.selectNodes("//recievingEndpoints[@endpointId='slavkovsky@orange.sk']/..");
    	assertThat(notificationIntentsForSlavkovsky.size()).isEqualTo(1);
    	    	
    	Message<?> notificationIntentForSlavkovsky = notificationIntentsForSlavkovsky.iterator().next();
    	context = JXPathContext.newContext(notificationIntentForSlavkovsky);
    	List<RecievingEndpoint> endpoints = context.selectNodes("/recievingEndpoints");
    	assertThat(endpoints.size()).isEqualTo(1);
    	
    	assertThat(endpoints.iterator().next().getRecipient().getName()).isEqualTo("Adrian Slavkovsky");
    	
    	TemplateWithModelEmailContent<SalesEventModel> msgContent = (TemplateWithModelEmailContent<SalesEventModel>)notificationIntentForSlavkovsky.getBody();
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
    	List<Message<?>> result = startOutageConverter.apply(event);
    	
    	//THEN
    	assertThat(result.size()).isEqualTo(8); //3xcustomers email,2xcustomer sms, 2xsales, 1xsales agent 
    	
    	//THEN check events for agent
    	JXPathContext context = JXPathContext.newContext(result);
		List<EmailMessageTemplated<?>> notificationIntentsForAgent = context.selectNodes("//recievingEndpoints[@endpointId='sales@objectify.sk']/..");
    	
    	assertThat(notificationIntentsForAgent.size()).isEqualTo(1);
    	
    	context = JXPathContext.newContext(notificationIntentsForAgent);
    	List<RecievingEndpoint> endpoints = context.selectNodes("/recievingEndpoints");
    	assertThat(endpoints.size()).isEqualTo(1);
    	
    	assertThat(endpoints.iterator().next().getRecipient()).isNull(); //Pre sales agentov nemam person
    	
    	EmailMessageTemplated<SalesAgentEventModel> notificationIntentForAgent = (EmailMessageTemplated<SalesAgentEventModel>)notificationIntentsForAgent.iterator().next();
    	TemplateWithModelEmailContent<SalesAgentEventModel> msgContent = notificationIntentForAgent.getBody();
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

