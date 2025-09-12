package com.steve.secure_auth.service;

import com.steve.secure_auth.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendVerificationEmail(User user, String token) {
        String subject = "Verify your email";
        String verificationUrl = "http://your-domain.com/api/auth/verify-email?token=" + token;

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

    // General-purpose method for sending emails
    public void sendEmail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        mailSender.send(message);
    }
}