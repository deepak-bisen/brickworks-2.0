package com.brickwork.users.service.user.impl;

import com.brickwork.users.service.user.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public EmailServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void sendOtp(String email, String otp) {

        try {
            log.info("Sending OTP mail to email address {}", email);
            SimpleMailMessage message = new SimpleMailMessage();


            message.setFrom(fromEmail);
            message.setTo(email);
            message.setSubject("BrickWorks Password Reset OTP");
            message.setText("Dear User,\n\n" +
                    "Your One-Time Password is: "+ otp + "This OTP is valid for 10 minutes\n" +
                    "If you didn't request this, simply ignore this email.\n" +
                    "\nThank You!\n\n" +
                    "Regards,\nBrickWorks Pro Team");
            log.info("OTO mail sent to email address {}", email);
            mailSender.send(message);

        } catch (MailSendException e) {
            log.info("OTO mail sent to email address {} failed", email);
            throw new RuntimeException("Failed to send OTP email", e);
        }
    }
}
