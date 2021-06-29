package com.obj.nc.domain.notifIntent;

import com.fasterxml.jackson.databind.JsonNode;
import com.obj.nc.utils.JsonUtils;
import org.apache.commons.lang3.NotImplementedException;
import org.springframework.data.relational.core.mapping.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.obj.nc.domain.BasePayload;
import com.obj.nc.domain.content.MessageContent;
import com.obj.nc.domain.content.TemplateWithModelContent;
import com.obj.nc.domain.content.email.EmailContent;
import com.obj.nc.domain.content.email.TemplateWithModelEmailContent;
import com.obj.nc.domain.content.mailchimp.MailchimpContent;
import com.obj.nc.domain.content.sms.SimpleTextContent;
import com.obj.nc.domain.endpoints.EmailEndpoint;
import com.obj.nc.domain.endpoints.MailchimpEndpoint;
import com.obj.nc.domain.endpoints.RecievingEndpoint;
import com.obj.nc.domain.endpoints.SmsEndpoint;
import com.obj.nc.domain.message.EmailMessage;
import com.obj.nc.domain.message.EmailMessageTemplated;
import com.obj.nc.domain.message.MailChimpMessage;
import com.obj.nc.domain.message.Message;
import com.obj.nc.domain.message.SmstMessage;
import com.obj.nc.domain.message.SmsMessageTemplated;
import com.obj.nc.domain.notifIntent.content.IntentContent;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.log4j.Log4j2;

import java.time.Instant;

import static com.obj.nc.flows.messageProcessing.MessageProcessingFlowConfig.MESSAGE_PROCESSING_FLOW_ID;

@Data
@NoArgsConstructor
@ToString(callSuper = false)
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
@Table("nc_intent")
@Log4j2
/**
 * This class represents Intent to deliver *some* kind of information at *some* point in time to recipient. Use this class in case
 * that you cannot tell the details about what/when/how are stored in delivery settings of that recipient. 
 * Example:
 * 		Recipient has two endpoints: Email, SMS
 * 		His settings are that he wants to be notified using SMS in working hours and by Email outside of working hours.
 * 		In addition his settings are that he want to receive at most 3 email in a week
 * 
 * Create notification intent and let NotiFlow to decide if 
 * 		SMS should be send immediately 
 * 			OR
 * 		Email should be send immediately
 * 			OR
 * 		Email should be send later in form of email aggregate
 * @author ja
 *
 * @param <BODY_TYPE>
 */
public class NotificationIntent extends BasePayload<IntentContent> {
	
	public static final String JSON_TYPE_IDENTIFIER = "INTENT";
	
	private String externalId;
	private Instant timeConsumed;
	
	@Override
	@JsonIgnore
	public String getPayloadTypeName() {
		return JSON_TYPE_IDENTIFIER;
	}
	
	public Message<?> createMessage(RecievingEndpoint endpointsForOneSubject) {
		MessageContent msgContent = getBody().createMessageContent(endpointsForOneSubject);

		if (msgContent instanceof EmailContent) {
			EmailMessage email = new EmailMessage();
			email.setBody((EmailContent)msgContent);
			
			return email;
		} 
		
		if (msgContent instanceof SimpleTextContent) {
			SmstMessage sms = new SmstMessage();
			sms.setBody((SimpleTextContent)msgContent);
			
			return sms;
		} 
		
		if (msgContent instanceof TemplateWithModelContent && endpointsForOneSubject instanceof EmailEndpoint) {
			EmailMessageTemplated<?> email = new EmailMessageTemplated<>();
			email.setBody((TemplateWithModelEmailContent)msgContent);
			
			return email;
		} 
		
		if (msgContent instanceof TemplateWithModelContent && endpointsForOneSubject instanceof SmsEndpoint) {
			SmsMessageTemplated<?> sms = new SmsMessageTemplated<>();
			sms.setBody((TemplateWithModelContent)msgContent);
			
			return sms;
		} 

		
		if (endpointsForOneSubject instanceof MailchimpEndpoint) {
			MailChimpMessage mailChimp = new MailChimpMessage();
			mailChimp.setBody((MailchimpContent)msgContent);
			
			return mailChimp;
		}

		throw new NotImplementedException("Add additional cases");

	}
	
	public static NotificationIntent from(JsonNode messageJson) {
		NotificationIntent intent = JsonUtils.readObjectFromJSON(messageJson, NotificationIntent.class);
		intent.getHeader().setFlowId(messageJson.get("flowId") != null ? messageJson.get("flowId").textValue() : MESSAGE_PROCESSING_FLOW_ID);
		return intent;
	}
	
	public void overrideFlowIdIfApplicable(String flowId) {
		if (flowId == null) {
			return;
		}
		header.setFlowId(flowId);
	}
	
	public void overrideExternalIdIfApplicable(String externalId) {
		if (externalId == null) {
			return;
		}
		this.externalId = externalId;
	}
	
}
