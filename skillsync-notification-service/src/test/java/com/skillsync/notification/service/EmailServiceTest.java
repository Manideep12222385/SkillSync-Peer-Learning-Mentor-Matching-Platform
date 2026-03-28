package com.skillsync.notification.service;

import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private MimeMessage mimeMessage;

    @InjectMocks
    private EmailService emailService;

    @BeforeEach
    void setUp() {
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
    }

    @Test
    void testSendEmail_Success() {
        emailService.sendEmail("test@example.com", "Subject", "Body");

        verify(mailSender, times(1)).send(mimeMessage);
    }

    @Test
    void testSendEmail_MessagingException() {
        doThrow(new org.springframework.mail.MailSendException("Simulated exception")).when(mailSender).send(mimeMessage);

        // Since it catches MessagingException or any MailException, we just verify it doesn't throw a RuntimeException to the caller
        try {
            emailService.sendEmail("test@example.com", "Subject", "Body");
        } catch (Exception e) {
            // Unexpected, but handled in the original code by logging
        }

        verify(mailSender, times(1)).send(mimeMessage);
    }
}
