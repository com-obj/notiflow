package com.obj.nc.functions.processors.messageTemplating.config;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

@ConfigurationProperties(prefix = "nc.functions.email-templates")
@Data
@Component
public class ThymeleafConfigProperties {

	private List<String> templatesRootDir;
	
	private String messagesDirAndBaseName;
	
	private List<String> defaultLocaleCodes;
}
