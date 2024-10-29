package com.shreyas.CloudDemo.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

//@Service
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    @Value("${MAIL_SERVICE_ENABLED}")
    private boolean IsMailServiceEnabled;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Async
    public CompletableFuture<Boolean> sendEmail(String emailId, String Subject, String Message) {
        try{
            if(!IsMailServiceEnabled){
                log.info("Mail service is disabled by developers.");
                return CompletableFuture.completedFuture(false);
            }
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper =new MimeMessageHelper(mimeMessage, StandardCharsets.UTF_8.name());
            helper.setTo(emailId);
            helper.setSubject(Subject);
            helper.setText(Message, true);
            helper.setFrom("spkothari968@gmail.com");
            mailSender.send(mimeMessage);
            log.info("Email for {} sent to {}",Subject, emailId);
            return CompletableFuture.completedFuture(true);
        }catch (MessagingException ex){
            log.error(ex.getMessage(), ex);
            return CompletableFuture.completedFuture(false);
        }

    }
}
