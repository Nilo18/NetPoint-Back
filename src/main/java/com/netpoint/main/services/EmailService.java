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
        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom("netpoint19923@gmail.com");
        message.setTo(userEmail);
        message.setSubject("Your NetPoint Security Code");
        message.setText("Hello! Your verification code is: " + otpCode +
                "\n\nIt will expire in 5 minutes.");

        mailSender.send(message);
    }
}
