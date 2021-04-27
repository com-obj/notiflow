package com.obj.nc.functions.processors.messageAggregator.aggregations;

import com.obj.nc.domain.BasePayload;
import com.obj.nc.domain.content.mailchimp.*;
import com.obj.nc.domain.endpoints.MailchimpEndpoint;
import com.obj.nc.domain.message.Message;
import com.obj.nc.exceptions.PayloadValidationException;
import com.obj.nc.functions.processors.senders.mailchimp.MailchimpSenderConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import java.util.*;
import java.util.stream.Collectors;

@Log4j2
@RequiredArgsConstructor
public class MailchimpMessageAggregationStrategy extends BasePayloadAggregationStrategy {
	
	private final MailchimpSenderConfig mailchimpSenderConfig;
	
	@Override
	protected Optional<PayloadValidationException> checkPreCondition(List<? extends BasePayload> payloads) {
		Optional<PayloadValidationException> exception = checkContentTypes(payloads, MailchimpContent.class);
		if (exception.isPresent()) {
			return exception;
		}
		
		exception = checkDeliveryOptions(payloads);
		if (exception.isPresent()) {
			return exception;
		}
		
		return checkReceivingEndpoints(payloads);
	}
	
	@Override
	protected Optional<PayloadValidationException> checkReceivingEndpoints(List<? extends BasePayload> payloads) {
		Optional<PayloadValidationException> exception = checkEndpointTypes(payloads, MailchimpEndpoint.class);
		if (exception.isPresent()) {
			return exception;
		}
		
		return super.checkReceivingEndpoints(payloads);
	}
	
	@Override
	public Object merge(List<? extends BasePayload> payloads) {
		if (payloads.isEmpty()) return null;
		
		MailchimpContent aggregatedMailchimpContent = payloads
				.stream()
				.map(BasePayload::<MailchimpContent>getContentTyped)
				.reduce(this::concatContents)
				.orElseThrow(() -> new RuntimeException(String.format("Could not aggregate input messages: %s", payloads)));
		
		//TODO: ked bude refactorovany header a ostatne veci tak tuto spravit novu message a neprepisovat existujucu
		Message outputMessage = (Message) payloads.get(0);
		outputMessage.getBody().setMessage(aggregatedMailchimpContent);
		return outputMessage;
	}
	
	private MailchimpContent concatContents(MailchimpContent one, MailchimpContent other) {
		MailchimpContent concated = new MailchimpContent();
		concated.setTemplateName(mailchimpSenderConfig.getAggregatedMessageTemplateName());
		
		List<MailchimpTemplateContent> templateContent = new ArrayList<>(one.getTemplateContent());
		templateContent.addAll(other.getTemplateContent());
		concated.setTemplateContent(templateContent);
		
		MailchimpMessage mailchimpMessage = new MailchimpMessage();
		mailchimpMessage.setTo(one.getMessage().getTo());
		mailchimpMessage.setSubject(mailchimpSenderConfig.getAggregatedMessageSubject());
		
		ArrayList<MailchimpAttachment> attachments = new ArrayList<>(one.getMessage().getAttachments());
		attachments.addAll(other.getMessage().getAttachments());
		mailchimpMessage.setAttachments(attachments);
		
		mailchimpMessage.setFromEmail(one.getMessage().getFromEmail());
		mailchimpMessage.setFromName(one.getMessage().getFromName());
		mailchimpMessage.setMergeLanguage(one.getMessage().getMergeLanguage());
		
		Map<String, List<MailchimpMergeVariable>> globalMergeCategoryValues = new HashMap<>();
		mailchimpSenderConfig.getMessageTypes().forEach(type -> globalMergeCategoryValues.put(type, new ArrayList<>()));
		
		one.getMessage().getGlobalMergeVars().forEach(mergeVar -> {
			globalMergeCategoryValues.get(mergeVar.getContent().getType()).add(mergeVar);
		});
		other.getMessage().getGlobalMergeVars().forEach(mergeVar -> {
			globalMergeCategoryValues.get(mergeVar.getContent().getType()).add(mergeVar);
		});
		mailchimpMessage.setGlobalMergeVars(globalMergeCategoryValues.entrySet().stream().map(this::mapMergeVar).collect(Collectors.toList()));
		return concated;
	}
	
	protected MailchimpMergeVariable mapMergeVar(Map.Entry<String, List<MailchimpMergeVariable>> entry) {
		MailchimpMergeVariable mergeVar = new MailchimpMergeVariable();
		mergeVar.setName(entry.getKey());
		
		AggregatedMailchimpData data = new AggregatedMailchimpData();
		data.setType(AggregatedMailchimpData.JSON_TYPE_NAME);
		data.setData(entry.getValue());
		
		mergeVar.setContent(data);
		return mergeVar;
	}

}