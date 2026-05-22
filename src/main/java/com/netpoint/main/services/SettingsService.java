package com.netpoint.main.services;

import com.netpoint.main.dto.CompanyDTO;
import com.netpoint.main.dto.UserDTO;
import com.netpoint.main.dto.requests.CashierAdditionRequest;
//import com.netpoint.main.dto.responses.CashierAdditionResponse;
import com.netpoint.main.dto.requests.CompanyUpdateRequest;
import com.netpoint.main.dto.responses.CompanyInfoChangeVerificationResponse;
import com.netpoint.main.dto.responses.UserModificationResponse;
import com.netpoint.main.exceptions.*;
import com.netpoint.main.models.Company;
import com.netpoint.main.models.OtpEntry;
import com.netpoint.main.models.User;
import com.netpoint.main.repositories.CompanyRepository;
import com.netpoint.main.repositories.UserRepository;
import lombok.Data;
import lombok.extern.java.Log;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.List;

@Service
@Data
@Log
public class SettingsService {
    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender;
    private final OtpStore otpStore;

    public Page<UserDTO> fetchCompanyUsers(Long id, Pageable pageable) {
        if (!companyRepository.existsById(id)) {
            throw new CompanyNotFoundException("Couldn't find company by id");
        }

        return userRepository.findByCompanyId_Id(id, pageable)
                .map(u -> new UserDTO(u.getId(), u.getName(), u.getEmail(), u.getRole()));
    }

    public Page<UserDTO> addCashier(CashierAdditionRequest cashier, Pageable pageable) {
        String role = cashier.role().trim().toLowerCase();

        if (!role.equals("cashier")) {
            throw new UnallowedRoleException("Only cashiers are allowed to be registered with a pin");
        }

        if (userRepository.existsByEmail(cashier.email())) {
            throw new EmailAlreadyExistsException("A user with this email already exists");
        }

        if (!cashier.pin().matches("\\d{6}")) {
            throw new InvalidPinException("Pin must be exactly 6 digits");
        }

        // ****
        // ADD A JWT AUTHORIZATION HERE LATER ON TO MAKE SURE THAT AN OWNER FROM
        // ANOTHER COMPANY DOESN'T ADD A CASHIER IN SOMEONE ELSE'S COMPANY
        // ****

        Company company = this.companyRepository.findById(Long.valueOf(cashier.companyId()))
                .orElseThrow(() -> new CompanyNotFoundException("Company with the given id was not found"));

        User user = new User();
        user.setName(cashier.name());
        user.setEmail(cashier.email());
        user.setRole(cashier.role());
        user.setCompanyId(company);
        user.setPin(passwordEncoder.encode(cashier.pin()));

        log.info("Saving " + user + " to the database...");

        this.userRepository.save(user);

        return userRepository.findByCompanyId_Id(Long.valueOf(cashier.companyId()), pageable)
                .map(u -> new UserDTO(u.getId(), u.getName(), u.getEmail(), u.getRole()));
    }

    public UserModificationResponse deleteUser(Integer userId) {
        // თუ იუზერი არ არსებობს, exception
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Suggested user was not found"));

        // Owner-ის წაშლა არ შეიძლება
        if (user.getRole().equals("OWNER")) {
            throw new InvalidRoleException("Cannot delete the owner account");
        }

        this.userRepository.delete(user);

        log.info("User deleted: " + userId);
        return new UserModificationResponse(
                200,
                new UserDTO(
                        user.getId(), user.getName(),
                        user.getEmail(), user.getRole()
                )
        );
    }

    public List<UserDTO> searchUser(String searchTerm, Long companyId) {
        log.info("Possible point before error.");
        companyRepository.findById(companyId)
                .orElseThrow(() -> new CompanyNotFoundException("Company not found"));

        log.info("The error should have been thrown, this shouldn't be printed");

        List<User> users = userRepository
                .searchByNameOrEmailWithinCompany(searchTerm, companyId);

        log.info("After searching users.");

        if (users.isEmpty()) throw new UserNotFoundException("No users found: " + searchTerm);

        return users.stream()
                .map(u -> new UserDTO(u.getId(), u.getName(), u.getEmail(), u.getRole()))
                .toList();
    }

    public CompanyDTO getCompanyById(Integer id) {
        Company company = companyRepository.findById(Long.valueOf(id))
                .orElseThrow(() -> new CompanyNotFoundException("Company not found"));

        return new CompanyDTO(
                company.getId(),
                company.getEmail(),
                company.getName(),
                company.getIndustry()
        );
    }

    public CompanyInfoChangeVerificationResponse verifyCompanyUpdateRequest(CompanyDTO suggested) {
        Company company = companyRepository.findById(Long.valueOf(suggested.id()))
                .orElseThrow(() -> new CompanyNotFoundException("Company not found"));

        String otp = String.valueOf(new SecureRandom().nextInt(900000) + 100000);

        String tempToken = otpStore.save(suggested.id().toString(), suggested.email(), otp);

        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom("netpoint19923@gmail.com");
        message.setTo(suggested.email());
        message.setSubject("Your Business Info Update Request Verification");
        message.setText("Hello! Your verification code is: " + otp +
                "\n\nIt will expire in 5 minutes.");

        mailSender.send(message);
        return new CompanyInfoChangeVerificationResponse(200, tempToken);
    }

    public CompanyDTO updateCompanyBusinessInfo(CompanyUpdateRequest suggested) {
        OtpEntry otpEntry = otpStore.get(suggested.verificationInfo().tempToken());

        if (!otpEntry.getOtpCode().equals(suggested.verificationInfo().otpCode())) {
//            otpStore.invalidate(suggested.verificationInfo().tempToken());
            throw new InvalidOtpException("Invalid verification code.");
        }

        if (otpEntry.isExpired()) {
//            log.info("Throwing verification code expired error...");
            throw new OtpExpiredException("Verification code expired.");
        }
        // ოტპ ვალიდურია და მაინც ინვალიდაციას უკეთებსბ
        otpStore.invalidate(suggested.verificationInfo().tempToken());

        Company company = companyRepository.findById(Long.valueOf(suggested.newInfo().id()))
                .orElseThrow(() -> new CompanyNotFoundException("Company not found"));

        company.setName(suggested.newInfo().name());
        company.setEmail(suggested.newInfo().email());
        company.setIndustry(suggested.newInfo().industry());

        Company saved = companyRepository.save(company);
        return new CompanyDTO(saved.getId(), saved.getEmail(), saved.getName(), saved.getIndustry());
    }
}
