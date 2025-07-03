package com.example.legaldocsimplifier.services.impl;

import com.example.legaldocsimplifier.services.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService {
    private final JavaMailSender mailSender;

    @Value("${spring.mail.to}")
    private String setTo;
    @Value("${spring.mail.from}")
    private String setFrom;

    @Autowired
    public EmailServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void sendEmail(String subject, String text, String userEmail) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(setTo);
        message.setSubject(subject);
        message.setText(text);
        message.setFrom(setFrom);
        message.setReplyTo(userEmail);
        mailSender.send(message);
    }
}
