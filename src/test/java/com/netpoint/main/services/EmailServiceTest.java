package com.netpoint.main.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

// MockitoExtension ავტომატურად ქმნის @Mockებს  და @InjectMocksს
@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailService emailService;

    @Test
    void sendOtpEmail_sendsToCorrectRecipient() {
        emailService.sendOtpEmail("user@test.com", "123456");

        // ArgumentCaptor იჭერს SimpleMailMessage რომელიც mailSender.send() რო გადაეცა
        ArgumentCaptor<SimpleMailMessage> captor =
                ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());

        SimpleMailMessage sent = captor.getValue();
        assertArrayEquals(new String[]{"user@test.com"}, sent.getTo());
    }

    @Test
    void sendOtpEmail_containsOtpCodeInBody() {
        emailService.sendOtpEmail("user@test.com", "847291");

        ArgumentCaptor<SimpleMailMessage> captor =
                ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());

        assertTrue(captor.getValue().getText().contains("847291"));
    }

    @Test
    void sendOtpEmail_hasCorrectSubject() {
        emailService.sendOtpEmail("user@test.com", "111222");

        ArgumentCaptor<SimpleMailMessage> captor =
                ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());

        assertEquals("Your NetPoint Security Code", captor.getValue().getSubject());
    }

    @Test
    void sendOtpEmail_senderIsNetpointEmail() {
        emailService.sendOtpEmail("user@test.com", "333444");

        ArgumentCaptor<SimpleMailMessage> captor =
                ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());

        // გამომგზავნი NetPoint-ის იმეილი უნდა იყოს, არა სხვა
        assertEquals("netpoint19923@gmail.com", captor.getValue().getFrom());
    }

    @Test
    void sendOtpEmail_callsSendExactlyOnce() {
        emailService.sendOtpEmail("user@test.com", "999000");
        // ზუსტად ერთხელ უნდა გაიგზავნოს
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }
}