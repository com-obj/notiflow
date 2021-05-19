package com.obj.nc.functions.processors.messageAggregator.aggregations;

import com.obj.nc.domain.BasePayload;
import com.obj.nc.domain.content.mailchimp.*;
import com.obj.nc.domain.endpoints.MailchimpEndpoint;
import com.obj.nc.domain.message.Message;
import com.obj.nc.exceptions.PayloadValidationException;
import com.obj.nc.functions.processors.senders.mailchimp.MailchimpSenderConfigProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import java.util.*;
import java.util.stream.Collectors;

@Log4j2
@RequiredArgsConstructor
public class MailchimpMessageAggregationStrategy extends BasePayloadAggregationStrategy {
	
	private final MailchimpSenderConfigProperties mailchimpSenderConfigProperties;
	
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
		MailchimpContent aggregatedContent = new MailchimpContent();
		aggregatedContent.setTemplateName(mailchimpSenderConfigProperties.getAggregatedMessageTemplateName());
		
		List<MailchimpTemplateContent> templateContent = new ArrayList<>(one.getTemplateContent());
		templateContent.addAll(other.getTemplateContent());
		aggregatedContent.setTemplateContent(templateContent);
		
		aggregatedContent.setRecipients(one.getRecipients());
		aggregatedContent.setSubject(mailchimpSenderConfigProperties.getAggregatedMessageSubject());
		
		ArrayList<MailchimpAttachment> attachments = new ArrayList<>(one.getAttachments());
		attachments.addAll(other.getAttachments());
		aggregatedContent.setAttachments(attachments);
		
		aggregatedContent.setSenderName(one.getSenderName());
		aggregatedContent.setSenderEmail(one.getSenderEmail());
		
		Map<String, List<Object>> globalMergeCategoryValues = new HashMap<>();
		mailchimpSenderConfigProperties.getMessageTypes().forEach(type -> globalMergeCategoryValues.put(type, new ArrayList<>()));
		
		aggregateGlobalMergeVariables(one, globalMergeCategoryValues);
		aggregateGlobalMergeVariables(other, globalMergeCategoryValues);
		aggregatedContent.setGlobalMergeVariables(globalMergeCategoryValues.entrySet().stream().map(this::mapMergeVar)
				.collect(Collectors.toList()));
		
		return aggregatedContent;
	}
	
	protected MailchimpMergeVariable mapMergeVar(Map.Entry<String, List<Object>> entry) {
		MailchimpMergeVariable mergeVar = new MailchimpMergeVariable();
		mergeVar.setName(entry.getKey());
		
		AggregatedMailchimpData data = new AggregatedMailchimpData();
		data.setSubject(mailchimpSenderConfigProperties.getAggregatedMessageSubject());
		data.setType(AggregatedMailchimpData.JSON_TYPE_NAME);
		data.setData(entry.getValue());
		
		mergeVar.setContent(data);
		return mergeVar;
	}
	
	private void aggregateGlobalMergeVariables(MailchimpContent content, Map<String, List<Object>> globalMergeCategoryValues) {
		content.getGlobalMergeVariables().forEach(mergeVar -> {
			if (mergeVar.getContent() instanceof AggregatedMailchimpData) {
				AggregatedMailchimpData aggregatedMailchimpData = (AggregatedMailchimpData) mergeVar.getContent();
				globalMergeCategoryValues.get(mergeVar.getName()).addAll(aggregatedMailchimpData.getData());
			} else {
				globalMergeCategoryValues.get(mergeVar.getName()).add(mergeVar.getContent());
			}
		});
	}

}