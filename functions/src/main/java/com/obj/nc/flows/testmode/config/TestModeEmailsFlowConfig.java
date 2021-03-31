package com.obj.nc.flows.testmode.config;

import static org.springframework.integration.dsl.MessageChannels.executor;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.aggregator.CorrelationStrategy;
import org.springframework.integration.aggregator.ReleaseStrategy;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.Pollers;
import org.springframework.integration.store.MessageGroup;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.support.PeriodicTrigger;

import com.obj.nc.flows.testmode.functions.processors.AggregateToSingleEmailTransformer;
import com.obj.nc.flows.testmode.functions.sources.GreenMailReceiverSourceSupplier;
import com.obj.nc.functions.processors.messageAggregator.MessageAggregator;
import com.obj.nc.functions.processors.messageAggregator.aggregations.MessageAggregationStrategy;
import com.obj.nc.functions.processors.messageAggregator.correlations.EventIdBasedCorrelationStrategy;
import com.obj.nc.functions.processors.messageTemplating.EmailTemplateFormatter;
import com.obj.nc.functions.processors.senders.EmailSender;
import com.obj.nc.functions.sink.payloadLogger.PaylaodLoggerSinkConsumer;

import lombok.extern.log4j.Log4j2;

@Configuration
@ConditionalOnProperty(value = "nc.flows.test-mode.enabled", havingValue = "true")
@Log4j2
public class TestModeEmailsFlowConfig {
	 
	@Autowired private TestModeProperties testModeProps;
	@Autowired private GreenMailReceiverSourceSupplier greenMailMessageSource;
    
    @Qualifier(TestModeEmailsBeansConfig.TEST_MODE_EMAIL_SENDER_FUNCTION_BEAN_NAME)
    @Autowired private EmailSender sendEmailRealSmtp;
    @Autowired private PaylaodLoggerSinkConsumer logConsumer;
    @Autowired private MessageAggregationStrategy aggregationStrategy;
    @Autowired private EmailTemplateFormatter digestEmailFormatter;
    
	
	public final static String TEST_MODE_GREEN_MAIL_SOURCE_BEAN_NAME = "tmGMSource";
	public final static String TEST_MODE_SOURCE_TRIGGER_BEAN_NAME = "tmSourceTrigger";
	public final static String TEST_MODE_AGGREGATOR_BEAN_NAME = "tmAggregator";
	public final static String TEST_MODE_THREAD_EXECUTOR_CHANNEL_NAME = "tmExecutorChannel";

    @Bean
    public IntegrationFlow testModeSendMessage() {
        return IntegrationFlows
        		.fromSupplier(greenMailMessageSource,
                      config -> config.poller(Pollers.trigger(testModeSourceTrigger()))
                      .id(TEST_MODE_GREEN_MAIL_SOURCE_BEAN_NAME))
        		.split()
        		.channel(executor(TEST_MODE_THREAD_EXECUTOR_CHANNEL_NAME, Executors.newSingleThreadExecutor()))
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
        		.transform(aggregateToSingleEmailTransformer())
        		.transform(digestEmailFormatter)
                .transform(sendEmailRealSmtp)
                .handle(logConsumer).get();
    }
    
    @Bean
    public CorrelationStrategy testModeCorrelationStrategy() {
    	return new EventIdBasedCorrelationStrategy(); //pull all in one group
    }
    
    @Bean
    public MessageAggregator testModeMessageAggregator() {
    	return new MessageAggregator(aggregationStrategy);
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


    @Bean(TEST_MODE_SOURCE_TRIGGER_BEAN_NAME)
    public Trigger testModeSourceTrigger() {
        return new PeriodicTrigger(testModeProps.getPollMockSourcesPeriodInSeconds(), TimeUnit.SECONDS);
    }
    
    @Bean
    public AggregateToSingleEmailTransformer aggregateToSingleEmailTransformer() {
    	return new AggregateToSingleEmailTransformer();
    }

}
