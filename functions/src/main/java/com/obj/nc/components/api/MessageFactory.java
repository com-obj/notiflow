package com.obj.nc.components.api;

import com.obj.nc.domain.endpoints.RecievingEndpoint;
import com.obj.nc.domain.message.EmailMessage;
import com.obj.nc.domain.message.EmailWithTestModeDiggest;
import com.obj.nc.domain.message.MailChimpMessage;
import com.obj.nc.domain.message.Message;
import com.obj.nc.domain.message.SimpleTextMessage;
import com.obj.nc.flows.testmode.email.functions.processors.TestModeDiggestMailContent;

public interface MessageFactory {

	public <T extends Message<?>> T createBasedOnEndpoint(Class<? extends RecievingEndpoint> endpointCls);

	public EmailMessage createAsEmail();

	public SimpleTextMessage createAsSms();
	
	public MailChimpMessage createAsMailChimp();
	
	public EmailWithTestModeDiggest createAsEmailWithTestModeDiggest(TestModeDiggestMailContent content);
}
