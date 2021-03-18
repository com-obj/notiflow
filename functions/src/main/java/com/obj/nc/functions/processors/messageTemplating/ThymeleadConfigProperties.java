package com.obj.nc.functions.processors.messageTemplating;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

@ConfigurationProperties(prefix = "nc.functions.email-templates")
@Data
@Component
public class ThymeleadConfigProperties {

	@Value("${templates-root-dir:message-templates}")
	private String templatesRootDir;
	

	@Value("${messages-dir-and-base-name:message-templates/messages}")
	private String messagesDirAndBaseName;
	
	@Value("${default-locale-codes:#{null}}")
	private List<String> defaultLocaleCodes;
}
