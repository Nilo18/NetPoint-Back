package com.netpoint.main.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    private JavaMailSender mailSender;
    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendOtpEmail(String userEmail, String otpCode) {
        sendMessage(
                "netpoint19923@gmail.com", userEmail, "Your NetPoint Security Code",
                "Hello! Your verification code is: " + otpCode +
                        "\n\nIt will expire in 5 minutes."
        );

    }

    public void sendOtpEmail(String userEmail, String otpCode, String subject) {
        sendMessage(
                "netpoint19923@gmail.com", userEmail, subject,
                "Hello! Your verification code is: " + otpCode +
                "\n\nIt will expire in 5 minutes."
        );
    }

    public void sendMessage(String from, String to, String subject, String msg) {
        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom(from);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(msg);

        mailSender.send(message);
    }
}
