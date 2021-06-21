package com.obj.nc.flows.testmode.config;

import static org.springframework.integration.dsl.MessageChannels.executor;

import java.util.concurrent.Executors;

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

import com.obj.nc.flows.testmode.TestModeProperties;
import com.obj.nc.flows.testmode.email.config.TestModeEmailsBeansConfig;
import com.obj.nc.functions.processors.messageAggregator.MessageAggregator;
import com.obj.nc.functions.processors.messageAggregator.aggregations.BasePayloadAggregationStrategy;
import com.obj.nc.functions.processors.messageAggregator.aggregations.TestModeSingleEmailAggregationStrategy;
import com.obj.nc.functions.processors.messageAggregator.correlations.EventIdBasedCorrelationStrategy;
import com.obj.nc.functions.processors.messageTemplating.EmailTemplateFormatter;
import com.obj.nc.functions.processors.senders.EmailSender;
import com.obj.nc.functions.sink.payloadLogger.PaylaodLoggerSinkConsumer;

import lombok.extern.log4j.Log4j2;

@Configuration
@ConditionalOnProperty(value = "nc.flows.test-mode.enabled", havingValue = "true")
@Log4j2
public class TestModeFlowConfig {

	@Autowired private TestModeProperties testModeProps;
    @Qualifier(TestModeEmailsBeansConfig.TEST_MODE_EMAIL_SENDER_FUNCTION_BEAN_NAME)
    @Autowired private EmailSender sendEmailRealSmtp;
    @Autowired private PaylaodLoggerSinkConsumer logConsumer;

    @Autowired private EmailTemplateFormatter digestEmailFormatter;

	public final static String TEST_MODE_THREAD_EXECUTOR_CHANNEL_NAME = "tmExecutorChannel";
	public final static String TEST_MODE_AGGREGATOR_BEAN_NAME = "tmAggregator";
	public final static String TEST_MODE_AGGREGATE_AND_SEND_FLOW_NAME = "testModeAggregateAndSendMessage";
    
    @Bean(TEST_MODE_AGGREGATE_AND_SEND_FLOW_NAME)
    public IntegrationFlow testModeAggregateAndSendMessage() {
    	printBanner();
    	
        return IntegrationFlows
        		.from(executor(TEST_MODE_THREAD_EXECUTOR_CHANNEL_NAME, Executors.newSingleThreadExecutor()))
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
        		.handle(digestEmailFormatter)
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
