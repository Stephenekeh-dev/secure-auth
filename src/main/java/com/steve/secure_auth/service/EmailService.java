package com.steve.secure_auth.service;

import com.steve.secure_auth.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendVerificationEmail(User user, String token) {
        String subject = "Verify your email";
        String verificationUrl = baseUrl + "/api/auth/verify-email?token=" + token;

        String message = "Hi " + user.getFirstname() + ",\n\n" +
                "Please verify your email by clicking the link below:\n" +
                verificationUrl + "\n\n" +
                "Thank you!";

        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(user.getEmail());
        mailMessage.setSubject(subject);
        mailMessage.setText(message);

        mailSender.send(mailMessage);
    }

    public void sendEmail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        mailSender.send(message);
    }
    public void sendPasswordResetEmail(User user, String token) {
        String subject = "Reset your password";
        String resetUrl = baseUrl + "/api/auth/reset-password?token=" + token;

        String message = "Hi " + user.getFirstname() + ",\n\n" +
                "You requested a password reset. Click the link below to reset your password:\n" +
                resetUrl + "\n\n" +
                "This link expires in 30 minutes.\n\n" +
                "If you did not request this, please ignore this email.\n\n" +
                "Thank you!";

        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(user.getEmail());
        mailMessage.setSubject(subject);
        mailMessage.setText(message);
        mailSender.send(mailMessage);
    }
}