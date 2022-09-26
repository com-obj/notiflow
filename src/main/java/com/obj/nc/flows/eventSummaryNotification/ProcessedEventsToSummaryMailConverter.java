package com.obj.nc.flows.eventSummaryNotification;

import com.obj.nc.domain.IsNotification;
import com.obj.nc.domain.content.email.TemplateWithModelEmailContent;
import com.obj.nc.domain.dto.DeliveryStatsByEndpointType;
import com.obj.nc.domain.endpoints.EmailEndpoint;
import com.obj.nc.domain.event.GenericEvent;
import com.obj.nc.domain.message.EmailMessageTemplated;
import com.obj.nc.domain.pullNotifData.PullNotifData;
import com.obj.nc.exceptions.PayloadValidationException;
import com.obj.nc.extensions.converters.pullNotifData.PullNotifData2NotificationConverterExtension;
import com.obj.nc.flows.eventSummaryNotification.model.SummaryEmailModel;
import com.obj.nc.repositories.GenericEventRepository;
import lombok.RequiredArgsConstructor;

import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class ProcessedEventsToSummaryMailConverter implements PullNotifData2NotificationConverterExtension<GenericEvent> {
	
	private final EventSummaryNotificationProperties properties;
	private final GenericEventRepository eventRepo;
	
	@Override
	public Optional<PayloadValidationException> canHandle(PullNotifData<GenericEvent> data) {
        if (data.getPayloads() != null) {
            return Optional.empty();
        }
        
		return Optional.of(new PayloadValidationException("ProcessedEventsToSummaryMailConverter only handles payloads of type GenericEvent"));
	}
	
	@Override
	public List<IsNotification> convert(PullNotifData<GenericEvent> data) {
		List<GenericEvent> eventFinished = data.getPayloads();
		List<IsNotification> resultEmails = new ArrayList<>();

		for (GenericEvent event: eventFinished) {
			List<DeliveryStatsByEndpointType> eventStats = eventRepo
				.findEventStatsByEndpointType(event.getId());
			
			//this doesn't need to be here. Pull flow should make sure not to notify twice.
			event.setNotifyAfterProcessing(false);				
			eventRepo.save(event);

			TemplateWithModelEmailContent<SummaryEmailModel> emailContent = new TemplateWithModelEmailContent<>();
			emailContent.setRequiredLocales(Arrays.asList(new Locale("en")));
			emailContent.setSubjectResourceKey("emailSummary.title");
			emailContent.setTemplateFileName(properties.getEmailTemplateFileName());

			SummaryEmailModel emailModel = SummaryEmailModel.builder()
					.eventStatsByType(eventStats)
					.eventName(event.getName())
					.eventDescription(event.getDescription())
					.build();
			emailContent.setModel(emailModel);

			EmailMessageTemplated<SummaryEmailModel> message = new EmailMessageTemplated<>(emailContent);

			getRecipients(event).forEach(message::addReceivingEndpoints);

			resultEmails.add(message);
		}

		return resultEmails;
	}

	protected List<EmailEndpoint> getRecipients(GenericEvent event) {
		return properties.getEmailRecipients().stream()
				.map(EmailEndpoint::new)
				.collect(Collectors.toList());
	}

	@Override
	public Class<GenericEvent> getPayloadType() {
		return GenericEvent.class;
	}
	
}
