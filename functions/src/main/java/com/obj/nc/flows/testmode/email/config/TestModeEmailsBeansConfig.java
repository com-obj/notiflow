package com.obj.nc.flows.testmode.email.config;

import java.util.Objects;
import java.util.Properties;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetup;
import com.obj.nc.functions.processors.senders.EmailSender;

@Configuration
@ConditionalOnProperty(value = "nc.flows.test-mode.enabled", havingValue = "true")
public class TestModeEmailsBeansConfig {
	
    public static final String TEST_MODE_REAL_MAIL_SENDER_BEAN_NAME = "testModeJavaMailSenderReal";
    public static final String TEST_MODE_EMAIL_SENDER_FUNCTION_BEAN_NAME = "testModeEmailSenderReal";   
    public static final String TEST_MODE_GREEN_MAIL_BEAN_NAME = "testModeGreenMail";
    
    
    private @Autowired TestModeGreenMailProperties properties;

	/**
     * This mail sender will be used to send the aggregated email composed of email received by the TEST_MODE_GREEN_MAIL_BEAN_NAME
     * @param environment
     * @return
     */
    @Bean(TEST_MODE_REAL_MAIL_SENDER_BEAN_NAME)
    public JavaMailSenderImpl testModeJavaMailSenderReal(Environment environment) {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(environment.getProperty("spring.mail.host"));
        mailSender.setPort(Integer.parseInt(Objects.requireNonNull(environment.getProperty("spring.mail.port"))));
        mailSender.setUsername(environment.getProperty("spring.mail.username"));
        mailSender.setPassword(environment.getProperty("spring.mail.password"));
        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.default-encoding", "UTF-8");
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", environment.getProperty("spring.mail.properties.mail.smtp.auth","true"));
        props.put("mail.smtp.starttls.enable", environment.getProperty("spring.mail.properties.mail.smtp.starttls.enable","true"));
        mailSender.setJavaMailProperties(props);
        return mailSender;
    }
 
    
    /**
     * This is default mail server override. Instead of using the spring.mail* properties to configure real mail sender,
     * setup mail sender in such a way that it send email to GreenMail specific for test mode
     * This bean is initialized only if nc.flows.test-mode.enabled = true. Otherwise the default bean initialized by spring is used
     * @param properties
     * @return
     */
    @Bean
    @Primary
    public JavaMailSenderImpl mailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost("localhost");
        mailSender.setPort(properties.getSmtpPort());
        mailSender.setUsername(TEST_MODE_GREEN_MAIL_USER);
        mailSender.setPassword(TEST_MODE_GREEN_MAIL_PDW);
        
        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.default-encoding", "UTF-8");
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.debug", "false");
        props.put("mail.smtp.auth", "false");
        mailSender.setJavaMailProperties(props);
        
        return mailSender;

    }

    @Bean(TEST_MODE_EMAIL_SENDER_FUNCTION_BEAN_NAME)
    public EmailSender testModeEmailSenderSinkProcessingFunction(
    		@Qualifier(TEST_MODE_REAL_MAIL_SENDER_BEAN_NAME) JavaMailSenderImpl javaMailSender) {
        return new EmailSender(javaMailSender);
    }
  
    public static final String TEST_MODE_GREEN_MAIL_USER = "testmode@objectify.sk";
    public static final String TEST_MODE_GREEN_MAIL_PDW = "xxx";
    /**
     * This GreenMail will start on SMTP port and will catch all email which would normally be send to standard
     * spring.mail end point. This is achieved by redirecting mailSender to this mail server
     * @return
     */
    @Bean(value=TEST_MODE_GREEN_MAIL_BEAN_NAME, destroyMethod="stop")
    public GreenMail testModeSMTPServer() {
        ServerSetup SMTP = new ServerSetup(properties.getSmtpPort(), null, ServerSetup.PROTOCOL_SMTP);
//        ServerSetup IMAP = new ServerSetup(properties.getImapPort(), null, ServerSetup.PROTOCOL_IMAP);
    	ServerSetup[] setups = new ServerSetup[]{SMTP};
    	
		GreenMail gm = new GreenMail(setups);
		gm.setUser(TEST_MODE_GREEN_MAIL_USER, TEST_MODE_GREEN_MAIL_PDW);
		gm.start();
		return gm;
    }
    
}
