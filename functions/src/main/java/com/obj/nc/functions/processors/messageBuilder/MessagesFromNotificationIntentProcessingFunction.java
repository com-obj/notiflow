package com.obj.nc.functions.processors.messageBuilder;

import com.obj.nc.domain.message.Message;
import com.obj.nc.domain.notifIntent.NotificationIntent;
import com.obj.nc.functions.PreCondition;
import com.obj.nc.functions.processors.ProcessorFunction;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Function;

@Component
@AllArgsConstructor
public class MessagesFromNotificationIntentProcessingFunction extends ProcessorFunction<NotificationIntent, List<Message>> {

	@Autowired
	private MessagesFromNotificationIntentExecution execution;

	@Autowired
	private MessagesFromNotificationIntentPreCondition preCondition;

	@Override
	public PreCondition<NotificationIntent> preCondition() {
		return preCondition;
	}

	@Override
	public Function<NotificationIntent, List<Message>> execution() {
		return execution;
	}

}
