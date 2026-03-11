package com.steve.secure_auth.service;

import com.steve.secure_auth.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EmailService Tests")
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailService emailService;

    @BeforeEach
    void setUp() {
        // Inject @Value field since we're not using Spring context
        ReflectionTestUtils.setField(emailService, "baseUrl", "http://localhost:8080");
    }

    // ─────────────────────────────────────────────
    // sendVerificationEmail
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("sendVerificationEmail — sends email to correct recipient")
    void sendVerificationEmail_sendsToCorrectRecipient() {
        User user = new User();
        user.setEmail("steve@example.com");
        user.setFirstname("Steve");

        emailService.sendVerificationEmail(user, "test-token");

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender, times(1)).send(captor.capture());

        SimpleMailMessage sent = captor.getValue();
        assertThat(sent.getTo()).containsExactly("steve@example.com");
    }

    @Test
    @DisplayName("sendVerificationEmail — sets correct subject")
    void sendVerificationEmail_setsCorrectSubject() {
        User user = new User();
        user.setEmail("steve@example.com");
        user.setFirstname("Steve");

        emailService.sendVerificationEmail(user, "test-token");

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());

        assertThat(captor.getValue().getSubject()).isEqualTo("Verify your email");
    }

    @Test
    @DisplayName("sendVerificationEmail — body contains verification URL with token")
    void sendVerificationEmail_bodyContainsVerificationUrl() {
        User user = new User();
        user.setEmail("steve@example.com");
        user.setFirstname("Steve");

        emailService.sendVerificationEmail(user, "abc-123");

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());

        String body = captor.getValue().getText();
        assertThat(body).contains("http://localhost:8080/api/auth/verify-email?token=abc-123");
    }

    @Test
    @DisplayName("sendVerificationEmail — body contains user's first name")
    void sendVerificationEmail_bodyContainsFirstName() {
        User user = new User();
        user.setEmail("steve@example.com");
        user.setFirstname("Steve");

        emailService.sendVerificationEmail(user, "abc-123");

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());

        assertThat(captor.getValue().getText()).contains("Hi Steve");
    }

    @Test
    @DisplayName("sendVerificationEmail — mailSender.send() called exactly once")
    void sendVerificationEmail_callsMailSenderOnce() {
        User user = new User();
        user.setEmail("steve@example.com");
        user.setFirstname("Steve");

        emailService.sendVerificationEmail(user, "token-xyz");

        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    // ─────────────────────────────────────────────
    // sendEmail
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("sendEmail — sends to correct recipient")
    void sendEmail_sendsToCorrectRecipient() {
        emailService.sendEmail("bob@example.com", "Hello", "World");

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());

        assertThat(captor.getValue().getTo()).containsExactly("bob@example.com");
    }

    @Test
    @DisplayName("sendEmail — sets correct subject and text")
    void sendEmail_setsCorrectSubjectAndText() {
        emailService.sendEmail("bob@example.com", "My Subject", "My Text");

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());

        SimpleMailMessage sent = captor.getValue();
        assertThat(sent.getSubject()).isEqualTo("My Subject");
        assertThat(sent.getText()).isEqualTo("My Text");
    }

    @Test
    @DisplayName("sendEmail — mailSender.send() called exactly once")
    void sendEmail_callsMailSenderOnce() {
        emailService.sendEmail("bob@example.com", "Subject", "Text");

        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    @DisplayName("sendEmail — mailSender throws, exception propagates")
    void sendEmail_mailSenderThrows_exceptionPropagates() {
        doThrow(new RuntimeException("SMTP error")).when(mailSender).send(any(SimpleMailMessage.class));

        org.junit.jupiter.api.Assertions.assertThrows(RuntimeException.class, () ->
                emailService.sendEmail("bob@example.com", "Subject", "Text"));
    }
}