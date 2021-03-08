package com.obj.nc.testmode.functions.processors;

import com.obj.nc.functions.processors.senders.EmailSenderSinkExecution;
import com.obj.nc.functions.processors.senders.EmailSenderSinkMicroService;
import com.obj.nc.functions.processors.senders.EmailSenderSinkPreCondition;
import com.obj.nc.functions.processors.senders.EmailSenderSinkProcessingFunction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@Configuration
@ConditionalOnProperty(value = "testmode.enabled", havingValue = "true")
public class TestModeEmailSenderConfig {

//    public static final String TEST_MODE_JAVA_MAIL_SENDER = "testmodeJavaMailSender";
//    public static final String TEST_MODE_EMAIL_SENDER_EXECUTION = "testmodeEmailSenderExecution";
//    public static final String TEST_MODE_EMAIL_SENDER_PRECONDITION = "testmodeEmailSenderPreCondition";
//    public static final String TEST_MODE_EMAIL_SENDER_PROCESSING_FUNCTION = "testmodeEmailSenderProcessingFunction";
    public static final String TEST_MODE_EMAIL_SENDER_MICRO_SERVICE = "testmodeSendMessage";

    @Autowired
    private TestModeEmailSenderProperties properties;

    @Autowired
    private EmailSenderSinkExecution.SendEmailMessageConfig config;

//    @Bean(TEST_MODE_JAVA_MAIL_SENDER)
    private JavaMailSender testmodeJavaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(properties.getHost());
        mailSender.setPort(properties.getPort());
        mailSender.setUsername(properties.getUsername());
        mailSender.setPassword(properties.getPassword());
        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.default-encoding", "UTF-8");
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.debug", "true");
        mailSender.setJavaMailProperties(props);
        return mailSender;
    }

//    @Bean(TEST_MODE_EMAIL_SENDER_EXECUTION)
    private EmailSenderSinkExecution execution() {
        return new EmailSenderSinkExecution(testmodeJavaMailSender(), config);
    }

//    @Bean(TEST_MODE_EMAIL_SENDER_PRECONDITION)
    private EmailSenderSinkPreCondition preCondition() {
        return new EmailSenderSinkPreCondition();
    }

//    @Bean(TEST_MODE_EMAIL_SENDER_PROCESSING_FUNCTION)
    private EmailSenderSinkProcessingFunction processingFunction() {
        return new EmailSenderSinkProcessingFunction(execution(), preCondition());
    }

    @Bean(TEST_MODE_EMAIL_SENDER_MICRO_SERVICE)
    public EmailSenderSinkMicroService microService() {
        return new EmailSenderSinkMicroService(processingFunction());
    }

}
