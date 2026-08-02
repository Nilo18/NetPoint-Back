package com.netpoint.main.services;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

@Service
public class EmailService {

    private static final String FROM_ADDRESS = "netpoint19923@gmail.com";
    private static final String OTP_TEMPLATE_PATH = "templates/email/otp-email.html";
    private static final String INVITE_TEMPLATE_PATH = "templates/email/invite-email.html";
    private static final String LOGO_PATH = "templates/email/images/logo.png";

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendOtpEmail(String userEmail, String otpCode) {
        sendOtpEmail(userEmail, otpCode, "Your NetPoint Security Code");
    }

    public void sendOtpEmail(String userEmail, String otpCode, String subject) {
        String html = buildOtpHtml(otpCode, userEmail);
        sendHtmlMessageWithInlineLogo(FROM_ADDRESS, userEmail, subject, html);
    }

    private String buildOtpHtml(String otpCode, String userEmail) {
        String template = loadTemplate(OTP_TEMPLATE_PATH);
        return template
                .replace("{{CODE}}", otpCode)
                .replace("{{USERNAME}}", userEmail);
    }

    public void sendInviteEmail(String userEmail, String companyName, String role, String link) {
        String html = buildInviteHtml(companyName, role, link);
        sendHtmlMessageWithInlineLogo(FROM_ADDRESS, userEmail, "You've Been Invited to NetPoint", html);
    }

    private String buildInviteHtml(String companyName, String role, String link) {
        String template = loadTemplate(INVITE_TEMPLATE_PATH);
        return template
                .replace("{{COMPANY}}", companyName)
                .replace("{{ROLE}}", role)
                .replace("{{LINK}}", link);
    }

    private String loadTemplate(String classpathLocation) {
        try {
            ClassPathResource resource = new ClassPathResource(classpathLocation);
            return new String(Files.readAllBytes(resource.getFile().toPath()), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Could not load email template: " + classpathLocation, e);
        }
    }

    public void sendHtmlMessage(String from, String to, String subject, String htmlBody) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, StandardCharsets.UTF_8.name());

            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true); // true = isHtml

            mailSender.send(mimeMessage);
        } catch (MessagingException e) {
            throw new IllegalStateException("Failed to send email to " + to, e);
        }
    }


    public void sendHtmlMessageWithInlineLogo(String from, String to, String subject, String htmlBody) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            // "true" here means multipart -> required so we can attach the inline image
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, StandardCharsets.UTF_8.name());

            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);

            ClassPathResource logo = new ClassPathResource(LOGO_PATH);
            helper.addInline("logo", logo); // "logo" here must match cid:logo in the HTML

            mailSender.send(mimeMessage);
        } catch (MessagingException e) {
            throw new IllegalStateException("Failed to send email to " + to, e);
        }
    }
}