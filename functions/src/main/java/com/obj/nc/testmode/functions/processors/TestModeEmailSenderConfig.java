package com.obj.nc.testmode.functions.processors;

import com.obj.nc.functions.processors.senders.EmailSenderSinkExecution;
import com.obj.nc.functions.processors.senders.EmailSenderSinkPreCondition;
import com.obj.nc.functions.processors.senders.EmailSenderSinkProcessingFunction;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Objects;
import java.util.Properties;

@Configuration
@ConditionalOnProperty(value = "testmode.enabled", havingValue = "true")
public class TestModeEmailSenderConfig {

    @Primary
    @Bean
    public JavaMailSenderImpl defaultMailSender(Environment environment) {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(environment.getProperty("spring.mail.host"));
        mailSender.setPort(Integer.parseInt(Objects.requireNonNull(environment.getProperty("spring.mail.port"))));
        mailSender.setUsername(environment.getProperty("spring.mail.username"));
        mailSender.setPassword(environment.getProperty("spring.mail.password"));
        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.default-encoding", "UTF-8");
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        mailSender.setJavaMailProperties(props);
        return mailSender;
    }

    @Bean
    public JavaMailSenderImpl testModeJavaMailSender(
            TestModeEmailSenderProperties properties
    ) {
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

    @Bean
    public EmailSenderSinkExecution testModeSendEmailExecution(
            @Qualifier("testModeJavaMailSender") JavaMailSenderImpl javaMailSender
    ) {
        return new EmailSenderSinkExecution(javaMailSender);
    }

    @Bean
    public EmailSenderSinkPreCondition testModeSendEmailPreCondition() {
        return new EmailSenderSinkPreCondition();
    }

    @Bean
    public EmailSenderSinkProcessingFunction testModeEmailSenderSinkProcessingFunction(
            @Qualifier("testModeSendEmailExecution") EmailSenderSinkExecution execution,
            @Qualifier("testModeSendEmailPreCondition") EmailSenderSinkPreCondition preCondition
    ) {
        return new EmailSenderSinkProcessingFunction(execution, preCondition);
    }

}
