package com.obj.nc.functions.processors.messageTemplating;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.util.CollectionUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.spring5.SpringTemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.thymeleaf.templateresolver.FileTemplateResolver;
import org.thymeleaf.templateresolver.ITemplateResolver;
import org.thymeleaf.templateresolver.StringTemplateResolver;

import lombok.Data;

@Configuration
@ConfigurationProperties(prefix = "nc.functions.email-templates")
@Data
public class ThymeleafConfiguration {

	private static final String EMAIL_TEMPLATE_ENCODING = "UTF-8";
	
	@Value("${templates-root-dir:message-templates}")
	private String templatesRootDir;
	
	@Value("${messages-dir-and-base-name:message-templates/messages}")
	private String messagesRootDir;
	
	@Value("${locales:#{null}}")
	private List<String> defaultLocaleCodes;
	
    @Autowired
    private SpringTemplateEngine templateEngine;

	@Bean
	public ResourceBundleMessageSource emailMessageSource() {
		final ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
		messageSource.setBasename(messagesRootDir);
		return messageSource;
	}
	
	
	@PostConstruct
    public TemplateEngine configure() {
        // Resolver for TEXT emails
        templateEngine.addTemplateResolver(textTemplateResolver());
        // Resolver for HTML emails (except the editable one)
        templateEngine.addTemplateResolver(htmlTemplateResolver());
        // Message source, internationalization specific to emails
        templateEngine.setTemplateEngineMessageSource(emailMessageSource());
        return templateEngine;
    }
    
    private ITemplateResolver textTemplateResolver() {
        final FileTemplateResolver templateResolver = new FileTemplateResolver();
        templateResolver.setOrder(Integer.valueOf(1));
        templateResolver.setPrefix(templatesRootDir + File.separator);
        templateResolver.setSuffix(".txt");
        templateResolver.setTemplateMode(TemplateMode.TEXT);
        templateResolver.setCharacterEncoding(EMAIL_TEMPLATE_ENCODING);
        templateResolver.setCacheable(false);
        return templateResolver;
    }

    private ITemplateResolver htmlTemplateResolver() {
        final FileTemplateResolver templateResolver = new FileTemplateResolver();
        templateResolver.setOrder(Integer.valueOf(2));
        templateResolver.setPrefix(templatesRootDir + File.separator);
        templateResolver.setSuffix(".html");
        templateResolver.setTemplateMode(TemplateMode.HTML);
        templateResolver.setCharacterEncoding(EMAIL_TEMPLATE_ENCODING);
        templateResolver.setCacheable(false);
        return templateResolver;
    }
    
    public List<Locale> getDefaultLocales() {
    	if (CollectionUtils.isEmpty(defaultLocaleCodes)) {
    		return Arrays.asList(Locale.getDefault());
    	}
    	
    	return defaultLocaleCodes.stream().map(locStr -> new Locale(locStr)).collect(Collectors.toList());
    }


}
