package com.obj.nc.functions.processors.messageBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.stereotype.Component;

import com.obj.nc.aspects.DocumentProcessingInfo;
import com.obj.nc.domain.content.Content;
import com.obj.nc.domain.endpoints.RecievingEndpoint;
import com.obj.nc.domain.message.Message;
import com.obj.nc.domain.notifIntent.NotificationIntent;
import com.obj.nc.exceptions.PayloadValidationException;
import com.obj.nc.functions.processors.ProcessorFunctionAdapter;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;

@Component
@AllArgsConstructor
@Log4j2
@DocumentProcessingInfo("GenerateMessagesFromIntent")
public class MessagesFromNotificationIntentProcessingFunction<CONTENT_TYPE extends Content> extends ProcessorFunctionAdapter<NotificationIntent<CONTENT_TYPE>, List<Message<CONTENT_TYPE>>> {

	@Override
	protected Optional<PayloadValidationException> checkPreCondition(NotificationIntent<CONTENT_TYPE> notificationIntent) {

		if (notificationIntent.getRecievingEndpoints().isEmpty()) {
			return Optional.of(new PayloadValidationException(
					String.format("NotificationIntent %s has no receiving endpoints defined.", notificationIntent)));
		}

		return Optional.empty();
	}

	@Override
	protected List<Message<CONTENT_TYPE>> execute(NotificationIntent<CONTENT_TYPE> notificationIntent) {
		log.debug("Create messages for {}",  notificationIntent);

		List<Message<CONTENT_TYPE>> messages = new ArrayList<>();

		for (RecievingEndpoint recievingEndpoint: notificationIntent.getRecievingEndpoints()) {

			Message<CONTENT_TYPE> msg = createBasedOnEndpoint(recievingEndpoint.getClass(), notificationIntent.getBody().getClass());
			
			msg.addRecievingEndpoints(recievingEndpoint);

			msg.setAttributes(notificationIntent.getAttributes());
			msg.setBody(notificationIntent.getBody());
			messages.add(msg);
		}

		return messages;
	}
	
	@SneakyThrows
	protected <T extends Message<?>> T createBasedOnEndpoint(Class<? extends RecievingEndpoint> endpointCls, Class<?> contentType) {
		List<Class<? extends Message<?>>> messageClasses =  findMessageSublasses();

		for (Class<? extends Message<?>> msgClass: messageClasses) {
			Message<?> msg = msgClass.newInstance();
			
			if (endpointCls.equals(msg.getRecievingEndpointType())) {
				return (T) msg;
			}
						
		}
		
		throw new IllegalArgumentException("Cannot infere message type from endpoint type: " + endpointCls.getName());
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
