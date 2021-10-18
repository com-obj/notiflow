/*
 *   Copyright (C) 2021 the original author or authors.
 *
 *   This file is part of Notiflow
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Lesser General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public License
 *   along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.obj.nc.flows.testmode.config;

import com.obj.nc.flows.testmode.TestModeProperties;
import com.obj.nc.flows.testmode.email.config.TestModeEmailsBeansConfig;
import com.obj.nc.functions.processors.endpointPersister.EndpointPersister;
import com.obj.nc.functions.processors.messageAggregator.MessageAggregator;
import com.obj.nc.functions.processors.messageAggregator.aggregations.BasePayloadAggregationStrategy;
import com.obj.nc.functions.processors.messageAggregator.aggregations.TestModeSingleEmailAggregationStrategy;
import com.obj.nc.functions.processors.messageAggregator.correlations.EventIdBasedCorrelationStrategy;
import com.obj.nc.functions.processors.messagePersister.MessagePersister;
import com.obj.nc.functions.processors.messageTemplating.EmailTemplateFormatter;
import com.obj.nc.functions.processors.senders.EmailSender;
import com.obj.nc.functions.sink.payloadLogger.PaylaodLoggerSinkConsumer;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.aggregator.CorrelationStrategy;
import org.springframework.integration.aggregator.ReleaseStrategy;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.store.MessageGroup;

import java.util.concurrent.Executors;

import static org.springframework.integration.dsl.MessageChannels.executor;

@Configuration
@ConditionalOnProperty(value = "nc.flows.test-mode.enabled", havingValue = "true")
@Log4j2
public class TestModeFlowConfig {

	@Autowired private TestModeProperties testModeProps;
    @Qualifier(TestModeEmailsBeansConfig.TEST_MODE_EMAIL_SENDER_FUNCTION_BEAN_NAME)
    @Autowired private EmailSender sendEmailRealSmtp;
    @Autowired private PaylaodLoggerSinkConsumer logConsumer;

    @Autowired private EmailTemplateFormatter digestEmailFormatter;
    @Autowired private EndpointPersister endpointPersister;
    @Autowired private MessagePersister messagePersister;

	public final static String TEST_MODE_THREAD_EXECUTOR_CHANNEL_NAME = "tmExecutorChannel";
	public final static String TEST_MODE_AGGREGATOR_BEAN_NAME = "tmAggregator";
	public final static String TEST_MODE_AGGREGATE_AND_SEND_FLOW_NAME = "testModeAggregateAndSendMessage";
    
    @Bean(TEST_MODE_AGGREGATE_AND_SEND_FLOW_NAME)
    public IntegrationFlow testModeAggregateAndSendMessage() {
    	printBanner();
    	
        return IntegrationFlows
        		.from(executor(TEST_MODE_THREAD_EXECUTOR_CHANNEL_NAME, Executors.newSingleThreadExecutor()))
				.handle(endpointPersister)
				.handle(messagePersister)
        		.aggregate(
        			aggSpec-> aggSpec
        				.correlationStrategy( testModeCorrelationStrategy() )
        				.releaseStrategy( testModeReleaseStrategy() )
        					.groupTimeout((testModeProps.getPollMockSourcesPeriodInSeconds()*2*1000)+500) //wait min 2 polls interval
        					.sendPartialResultOnExpiry(true)
        					.expireGroupsUponCompletion(true)
        					.expireGroupsUponTimeout(true)
        				.outputProcessor( testModeMessageAggregator() )
        				.id(TEST_MODE_AGGREGATOR_BEAN_NAME)
        			)
				.handle(endpointPersister)
				.handle(messagePersister)
				.handle(digestEmailFormatter)
				.handle(messagePersister)
				.handle(sendEmailRealSmtp)
                .handle(logConsumer)
                .get();
    }
    
    @Bean
    public CorrelationStrategy testModeCorrelationStrategy() {
    	return new EventIdBasedCorrelationStrategy(); //pull all in one group
    }
    
    @Bean
    public BasePayloadAggregationStrategy testModeAggregationStrategy() {
    	return new TestModeSingleEmailAggregationStrategy(testModeProps);
    }
    
    @Bean
    public MessageAggregator testModeMessageAggregator() {
    	return new MessageAggregator(testModeAggregationStrategy());
    }
    
	@Bean
	public ReleaseStrategy testModeReleaseStrategy() {
	  	return new NeverReleaseStrategy(); //based on timeout, not release strategy
	} 
	
	public static class NeverReleaseStrategy implements ReleaseStrategy {

		@Override
		public boolean canRelease(MessageGroup group) {
			return false;
		}

	}
	
	public static class LoggingSimpleSequenceSizeReleaseStrategy implements ReleaseStrategy {

		@Override
		public boolean canRelease(MessageGroup group) {
			boolean releasing = group.getSequenceSize() == group.size();
			
			log.info("Having {} sequence size, have {} group size, releasing: {}",group.getSequenceSize(),group.size(),releasing);
			return releasing;
		}

	}
	
	private void printBanner() {
		System.out.println(
				  "████████╗███████╗███████╗████████╗    ███╗   ███╗ ██████╗ ██████╗ ███████╗     █████╗  ██████╗████████╗██╗██╗   ██╗███████╗\r\n"
				+ "╚══██╔══╝██╔════╝██╔════╝╚══██╔══╝    ████╗ ████║██╔═══██╗██╔══██╗██╔════╝    ██╔══██╗██╔════╝╚══██╔══╝██║██║   ██║██╔════╝\r\n"
				+ "   ██║   █████╗  ███████╗   ██║       ██╔████╔██║██║   ██║██║  ██║█████╗      ███████║██║        ██║   ██║██║   ██║█████╗  \r\n"
				+ "   ██║   ██╔══╝  ╚════██║   ██║       ██║╚██╔╝██║██║   ██║██║  ██║██╔══╝      ██╔══██║██║        ██║   ██║╚██╗ ██╔╝██╔══╝  \r\n"
				+ "   ██║   ███████╗███████║   ██║       ██║ ╚═╝ ██║╚██████╔╝██████╔╝███████╗    ██║  ██║╚██████╗   ██║   ██║ ╚████╔╝ ███████╗\r\n"
				+ "   ╚═╝   ╚══════╝╚══════╝   ╚═╝       ╚═╝     ╚═╝ ╚═════╝ ╚═════╝ ╚══════╝    ╚═╝  ╚═╝ ╚═════╝   ╚═╝   ╚═╝  ╚═══╝  ╚══════╝\r\n"
				+ "                                                                                                                           ");
	}

}
