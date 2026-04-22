package com.netpoint.main.services;

import com.netpoint.main.models.Invitation;
import com.netpoint.main.models.User;
import com.netpoint.main.repositories.InvitationRepository;
import com.netpoint.main.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InvitationService {

    private final InvitationRepository invitationRepository;
    private final JavaMailSender mailSender;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    // owneri idzaxebs amas
    public void inviteUser(String email, String role, Integer companyId) {
        String token = UUID.randomUUID().toString();

        Invitation invitation = new Invitation();
        invitation.setEmail(email);
        invitation.setToken(token);
        invitation.setCompanyId(companyId);
        invitation.setRole(role);
        invitation.setExpiresAt(LocalDateTime.now().plusHours(48));
        invitation.setUsed(false);

        invitationRepository.save(invitation);
        sendInviteEmail(email, token, role);
    }

    private void sendInviteEmail(String email, String token, String role) {
        String link = frontendUrl + "/setup-account?token=" + token;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("You have been invited");
        message.setText(
                "You have been invited as " + role + ".\n\n" +
                        "Click the link below to set up your account (valid 48 hours):\n" +
                        link
        );

        mailSender.send(message);
    }

    // admini amas idzaxebs linkze gadasvlis mere
    public void completeRegistration(String token, String password, String fullName) {
        Invitation invitation = invitationRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid token"));

        if (invitation.isUsed()) {
            throw new RuntimeException("Invitation already used");
        }

        if (invitation.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Invitation expired");
        }

        // axal momxmarebels amatebs USER tables
        User user = new User();
        user.setEmail(invitation.getEmail());
        user.setPassword(passwordEncoder.encode(password));
        user.setName(fullName);          // setFullName -> setName
        user.setRole(invitation.getRole());
        user.setCompanyId(invitation.getCompanyId());
        userRepository.save(user);

        // tokens gamoyenebulze ayenebs
        invitation.setUsed(true);
        invitationRepository.save(invitation);
    }
}