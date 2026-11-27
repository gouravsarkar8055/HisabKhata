package com.user.config;

import java.util.Properties;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

@Configuration
public class MailConfig {

    @Bean
    public JavaMailSender javaMailSender() {
        JavaMailSenderImpl sender = new JavaMailSenderImpl();

        sender.setHost("smtp-relay.brevo.com");
        sender.setPort(587);
        sender.setUsername("9e1551001@smtp-brevo.com");
        sender.setPassword("xsmtpsib-49008123f2fb020f183e2be639500dc108005fd35e778ace662f82a22fe2870b-gHgtY9MYXysy9EFk");

        Properties props = sender.getJavaMailProperties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.starttls.required", "true");
        props.put("mail.smtp.ssl.trust", "smtp-relay.brevo.com");

        // 🔥 FORCE PLAIN
        props.put("mail.smtp.auth.mechanisms", "PLAIN");
        props.put("mail.smtp.auth.login.disable", "true");
        props.put("mail.smtp.auth.plain.enable", "true");

        props.put("mail.debug", "true");

        return sender;
    }
}

