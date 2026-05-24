package com.netpoint.main.services;

import com.netpoint.main.dto.responses.AuthResponse;
import com.netpoint.main.exceptions.*;
import com.netpoint.main.models.Company;
import com.netpoint.main.models.Invitation;
import com.netpoint.main.models.User;
import com.netpoint.main.repositories.CompanyRepository;
import com.netpoint.main.repositories.InvitationRepository;
import com.netpoint.main.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Log
public class InvitationService {

    private final InvitationRepository invitationRepository;
    private final JavaMailSender mailSender;
    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    // owneri idzaxebs amas
    public void inviteUser(String email, String role, Integer companyId) {
        Company company = this.companyRepository.findById(Long.valueOf(companyId))
                .orElseThrow(() -> new CompanyNotFoundException("Suggested company was not found"));

        if (!email.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
            throw new BadInvitationRequestException("Invalid email format");
        }

        boolean isOwner = this.userRepository.existsByEmailAndCompanyIdAndRole(email, company, "OWNER");

        if (isOwner) {
            throw new BadInvitationRequestException("Invited user must not be the owner");
        }

        boolean alreadyMember = this.userRepository.existsByEmailAndCompanyId(email, company);

        if (alreadyMember) {
            throw new BadInvitationRequestException("User is already a member of the company");
        }

        boolean alreadyInvited = this.invitationRepository.
                existsByEmailAndCompanyIdAndUsedFalseAndExpiresAtAfter(email, companyId, LocalDateTime.now());

        if (alreadyInvited) {
            throw new BadInvitationRequestException("User already has a pending invitation");
        }

//        List<String> validRoles = List.of("EMPLOYEE", "MANAGER", "ADMIN");
//        if (!validRoles.contains(role)) {
//            throw new BadInvitationRequestException("Invalid role: " + role);
//        }

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

    public String validateInvitation(String token) {
        Invitation suggestedInvitation = this.invitationRepository.findByToken(token)
                .orElseThrow(() -> new InvitationTokenNotFoundException("Invitation not found"));

        if (suggestedInvitation.isUsed()) {
            throw new InvitationTokenAlreadyUsedException("Invitation already used");
        }

        if (suggestedInvitation.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new InvitationTokenExpiredException("Invitation expired");
        }

        Company company = this.companyRepository
                .findById(Long.valueOf(suggestedInvitation.getCompanyId()))
                .orElseThrow(() -> new CompanyNotFoundException("Company not found"));
        return company.getName();
    }

    // admini amas idzaxebs linkze gadasvlis mere
    public AuthResponse completeRegistration(String token, String password, String fullName) {
        Invitation invitation = invitationRepository.findByToken(token)
                .orElseThrow(() -> new InvitationTokenNotFoundException("Invitation not found"));

        if (invitation.isUsed()) {
            throw new InvitationTokenAlreadyUsedException("Invitation already used");
        }

        if (invitation.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new InvitationTokenExpiredException("Invitation expired");
        }

        Company company = this.companyRepository.findById(Long.valueOf(invitation.getCompanyId()))
                .orElseThrow(() -> new CompanyNotFoundException("Company not found"));

        // axal momxmarebels amatebs USER tables

        User user = new User();
//        log.info("user values before using setters: " + user);
        user.setEmail(invitation.getEmail());
        user.setPassword(passwordEncoder.encode(password));
        user.setName(fullName);
        user.setRole(invitation.getRole());
        user.setCompany(company);
        log.info("invitation.getCompanyId() returns: " + invitation.getCompanyId());
        log.info("user.getCompanyId() returns: " + user.getCompany().getId());
        log.info("user values after using setters: " + user);

        userRepository.save(user);

        // tokens gamoyenebulze ayenebs
        invitation.setUsed(true);
        invitationRepository.delete(invitation);
        String jwt = jwtService.generateToken(
                String.valueOf(user.getId()), String.valueOf(user.getCompany().getId()),
                user.getEmail(), user.getName(), user.getRole()
        );
        return new AuthResponse("Valid", jwt);
    }
}