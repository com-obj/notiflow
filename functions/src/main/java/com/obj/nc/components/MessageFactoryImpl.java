package com.obj.nc.components;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.stereotype.Component;

import com.obj.nc.components.api.MessageFactory;
import com.obj.nc.domain.endpoints.RecievingEndpoint;
import com.obj.nc.domain.message.EmailMessage;
import com.obj.nc.domain.message.EmailWithTestModeDiggest;
import com.obj.nc.domain.message.MailChimpMessage;
import com.obj.nc.domain.message.Message;
import com.obj.nc.domain.message.SimpleTextMessage;
import com.obj.nc.flows.testmode.email.functions.processors.TestModeDiggestMailContent;

import lombok.SneakyThrows;

@Component
public class MessageFactoryImpl implements MessageFactory {

	@SneakyThrows
	public <T extends Message<?>> T createBasedOnEndpoint(Class<? extends RecievingEndpoint> endpointCls) {
		List<Class<? extends Message<?>>> messageClasses =  findMessageSublasses();

		for (Class<? extends Message<?>> msgClass: messageClasses) {
			Message<?> msg = msgClass.newInstance();
			
			if (endpointCls.equals(msg.getRecievingEndpointType())) {
				return (T) msg;
			}
		}
		
		throw new IllegalArgumentException("Cannot infere message type from endpoint type: " + endpointCls.getName());
	}

	public EmailMessage createAsEmail() {
		return new EmailMessage();
	}

	public SimpleTextMessage createAsSms() {
		return new SimpleTextMessage();		
	}
	
	public MailChimpMessage createAsMailChimp() {
		return new MailChimpMessage();
	}
	
	public EmailWithTestModeDiggest createAsEmailWithTestModeDiggest(TestModeDiggestMailContent content) {
		return new EmailWithTestModeDiggest(content);
	}
	
	@SneakyThrows
	private List<Class<? extends Message<?>>> findMessageSublasses() {
		ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(false);
		provider.addIncludeFilter(new AssignableTypeFilter(Message.class));

		List<Class<? extends Message<?>>> messageSubtypes = new ArrayList<>();
		Set<BeanDefinition> components = provider.findCandidateComponents("com/obj/nc");		
		for (BeanDefinition component : components)
		{
		    Class<? extends Message<?>> cls = (Class<? extends Message<?>>)Class.forName(component.getBeanClassName());
		    messageSubtypes.add(cls);
		}
		return messageSubtypes;
	}
}
