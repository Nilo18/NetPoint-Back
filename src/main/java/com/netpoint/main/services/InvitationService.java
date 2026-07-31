package com.netpoint.main.services;

import com.netpoint.main.dto.responses.AuthResponse;
import com.netpoint.main.dto.responses.GenericResponse;
import com.netpoint.main.exceptions.*;
import com.netpoint.main.models.AuditLog;
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
    private final AuditLogService auditLogService;
    private final EmailService emailService;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    // owneri idzaxebs amas
    public void inviteUser(Integer actorUserId, String email, String role, Integer companyId) {
        User actor = userRepository.findByIdAndCompany_Id(actorUserId, companyId)
                .orElseThrow(() -> new UserNotFoundException("Acting user was not found"));

        Company company = actor.getCompany();

        if (company == null) {
            throw new CompanyNotFoundException("Company was not found");
        }

        if (!email.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
            throw new BadInvitationRequestException("Invalid email format");
        }

        if (userRepository.existsByEmail(email)) {
            throw new BadInvitationRequestException("This email cannot be invited");
        }

        boolean isOwner = this.userRepository.existsByEmailAndCompany_IdAndRole(email, companyId, "OWNER");

        if (isOwner) {
            throw new BadInvitationRequestException("Invited user must not be the owner");
        }

        boolean alreadyMember = this.userRepository.existsByEmailAndCompany_Id(email, companyId);

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
        invitation.setSender(actor);
        invitation.setCompany(company);
        invitation.setRole(role);
        invitation.setExpiresAt(LocalDateTime.now().plusHours(48));
        invitation.setUsed(false);

        invitationRepository.save(invitation);
        sendInviteEmail(email, token, role);

        auditLogService.log(company, actor, AuditLog.EventType.USER_INVITED,
                "Invited user: " + email + " as " + role);
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

        if (userRepository.existsByEmail(suggestedInvitation.getEmail())) {
            throw new EmailAlreadyExistsException("Invitation cannot be accepted");
        }

        Company company = this.companyRepository
                .findById(suggestedInvitation.getCompany().getId())
                .orElseThrow(() -> new CompanyNotFoundException("Company not found"));
        return company.getName();
    }

    // admini amas idzaxebs linkze gadasvlis mere
    public GenericResponse completeRegistration(String token, String password, String fullName) {
        log.info("First attempt to catch StackOverflowError");
        Invitation invitation = invitationRepository.findByToken(token)
                .orElseThrow(() -> new InvitationTokenNotFoundException("Invitation not found"));

        if (invitation.isUsed()) {
            throw new InvitationTokenAlreadyUsedException("Invitation already used");
        }

        log.info("Second attempt to catch StackOverflowError");
        if (invitation.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new InvitationTokenExpiredException("Invitation expired");
        }

        if (userRepository.existsByEmail(invitation.getEmail())) {
            throw new EmailAlreadyExistsException("User with this email already exists");
        }

        log.info("Third attempt to catch StackOverflowError");
        Company company = this.companyRepository.findById(invitation.getCompany().getId())
                .orElseThrow(() -> new CompanyNotFoundException("Company not found"));

        // axal momxmarebels amatebs USER tables

        User user = new User();
//        log.info("user values before using setters: " + user);
        user.setEmail(invitation.getEmail());
        user.setPassword(passwordEncoder.encode(password));
        user.setName(fullName);
        user.setRole(invitation.getRole());
        user.setStatus(User.AccountStatus.PENDING_APPROVAL);
        user.setCompany(company);

        userRepository.save(user);

        // tokens gamoyenebulze ayenebs
        invitation.setUsed(true);
        invitationRepository.delete(invitation);

        User sender = userRepository.findById(invitation.getSender().getId())
                .orElseThrow(() -> new UserNotFoundException("Actor user was not found"));

        emailService.sendMessage(
                "netpoint19923@gmail.com", sender.getEmail(),
                "NetPoint User Invitation",
                user.getEmail() +
                """
                 has accepted your invite and is waiting to
                be accepted as a fully fledged member. If this is the email you wished to invite,
                you can accept them, if it is not, you can reject them and their account will be deleted.
                """
        );

        return new GenericResponse(
                200, "Registered successfully! Wait until the owner accepts your request."
        );
    }

    public GenericResponse acceptUser(Integer userToApproveId, Integer companyId) {
        User user = userRepository.findByIdAndCompany_Id(userToApproveId, companyId).
                orElseThrow(() -> new UserNotFoundException("User not found"));

        log.info("BEFORE SETTING STATUS: " + user.getStatus());
        user.setStatus(User.AccountStatus.ACTIVE);
        log.info("AFTER SETTING STATUS: " + user.getStatus());

        userRepository.save(user);

        return new GenericResponse(200, "User has been approved successfully!");
    }
}
