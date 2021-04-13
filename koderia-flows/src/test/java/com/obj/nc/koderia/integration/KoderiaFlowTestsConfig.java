package com.obj.nc.koderia.integration;

import com.obj.nc.functions.processors.senders.MailchimpSenderConfigProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.client.MockServerRestTemplateCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.test.web.client.MockRestServiceServer;

import com.obj.nc.koderia.config.RestClientConfig;
import com.obj.nc.koderia.services.MailchimpRestClientImpl;

@TestConfiguration
@Import(RestClientConfig.class)
@EnableConfigurationProperties(MailchimpSenderConfigProperties.class)
public class KoderiaFlowTestsConfig {

	@Autowired
	private MockServerRestTemplateCustomizer customizer;

	@Autowired
	private MailchimpRestClientImpl mailchimpServiceRest;

	@Primary
	@Bean
	public MockRestServiceServer mockMailchimpRestServer() {
		return customizer.getServer(mailchimpServiceRest.getRestTemplate());
	}

}

