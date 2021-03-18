package com.obj.nc.functions.processors.messageTemplating;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.util.CollectionUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.spring5.SpringTemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.FileTemplateResolver;
import org.thymeleaf.templateresolver.ITemplateResolver;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.log4j.Log4j2;

@Configuration
@Data
@Log4j2
@AllArgsConstructor
public class ThymeleafConfiguration {

	private static final String EMAIL_TEMPLATE_ENCODING = "UTF-8";
	
   	private ThymeleadConfigProperties config;
    private SpringTemplateEngine templateEngine;

	@Bean(name = "nc.emailTemplateFormatter.messageSource")
	public ReloadableResourceBundleMessageSource emailMessageSource() {
		log.info("Configuring i18n message source to be have basename (path end file name prefix) " + config.getMessagesDirAndBaseName());
		
		final ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
		messageSource.setBasename("file:" + config.getMessagesDirAndBaseName());
		messageSource.setDefaultEncoding("UTF-8");
		return messageSource;
	}
	
	
	@PostConstruct
	@Bean(name = "nc.emailTemplateFormatter.templateEngine")
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
		log.info("Configuring Thymeleaf template resolver root path to be " + config.getTemplatesRootDir()  + File.separator);
		
        final FileTemplateResolver templateResolver = new FileTemplateResolver();
        templateResolver.setOrder(Integer.valueOf(1));
        templateResolver.setPrefix(config.getTemplatesRootDir() + File.separator);
        templateResolver.setSuffix(".txt");
        templateResolver.setTemplateMode(TemplateMode.TEXT);
        templateResolver.setCharacterEncoding(EMAIL_TEMPLATE_ENCODING);
        templateResolver.setCacheable(false);
        return templateResolver;
    }

    private ITemplateResolver htmlTemplateResolver() {
		log.info("Configuring Thymeleaf template resolver root path to be " + config.getTemplatesRootDir()  + File.separator);
		
        final FileTemplateResolver templateResolver = new FileTemplateResolver();
        templateResolver.setOrder(Integer.valueOf(2));
        templateResolver.setPrefix(config.getTemplatesRootDir()  + File.separator);
        templateResolver.setSuffix(".html");
        templateResolver.setTemplateMode(TemplateMode.HTML);
        templateResolver.setCharacterEncoding(EMAIL_TEMPLATE_ENCODING);
        templateResolver.setCacheable(false);
        return templateResolver;
    }
    
    public List<Locale> getDefaultLocales() {
    	if (CollectionUtils.isEmpty(config.getDefaultLocaleCodes())) {
    		return Arrays.asList(Locale.getDefault());
    	}
    	
    	return config.getDefaultLocaleCodes().stream().map(locStr -> new Locale(locStr)).collect(Collectors.toList());
    }


}
