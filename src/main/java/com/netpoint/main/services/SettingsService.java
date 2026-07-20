package com.netpoint.main.services;

import com.netpoint.main.dto.CompanyDTO;
import com.netpoint.main.dto.UserDTO;
import com.netpoint.main.dto.requests.CashierAdditionRequest;
import com.netpoint.main.dto.requests.ModifyProductRequest;
import com.netpoint.main.repositories.*;
import com.netpoint.main.dto.requests.CompanyUpdateRequest;
import com.netpoint.main.dto.responses.InfoChangeVerificationResponse;
import com.netpoint.main.dto.responses.UserModificationResponse;
import com.netpoint.main.exceptions.*;
import com.netpoint.main.models.AuditLog;
import com.netpoint.main.models.Company;
import com.netpoint.main.models.OtpEntry;
import com.netpoint.main.models.ProductAttribute;
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
import com.netpoint.main.dto.requests.UpdateAccountRequest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

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
    private final ProductRepository productRepository;
    private final ProductAttributeRepository productAttributeRepository;
    private final ProductAttributeValueRepository productAttributeValueRepository;
    private final AuditLogService auditLogService;
    private final AuditLogRepository auditLogRepository;
    private final SaleRepository saleRepository;
    private final SupabaseStorageService supabaseStorageService;

    public Page<UserDTO> fetchCompanyUsers(Integer id, Pageable pageable) {
        log.info("LOOKING FOR COMPANY WITH ID: " + id);
        if (!companyRepository.existsById(id)) {
            throw new CompanyNotFoundException("Couldn't find company by id");
        }

        return userRepository.findByCompany_Id(id, pageable)  // <-- changed
                .map(u -> new UserDTO(u.getId(), u.getName(), u.getEmail(), u.getRole()));
    }

    public Page<UserDTO> addCashier(Integer actorUserId, CashierAdditionRequest cashier, Pageable pageable) {
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

        Company company = this.companyRepository.findById(cashier.companyId())
                .orElseThrow(() -> new CompanyNotFoundException("Company with the given id was not found"));

        User user = new User();
        user.setName(cashier.name());
        user.setEmail(cashier.email());
        user.setRole(cashier.role());
        user.setCompany(company);
//        user.setPin(passwordEncoder.encode(cashier.pin()));

        log.info("Saving " + user + " to the database...");

        this.userRepository.save(user);

        User actor = userRepository.findById(actorUserId)
                .orElseThrow(() -> new UserNotFoundException("Acting user was not found"));
        auditLogService.log(company, actor, AuditLog.EventType.TEAM_MEMBER_ADDED,
                "Added team member: " + cashier.email() + " (" + cashier.role() + ")");

        return userRepository.findByCompany_Id(cashier.companyId(), pageable)
                .map(u -> new UserDTO(u.getId(), u.getName(), u.getEmail(), u.getRole()));
    }

    @Transactional
    public UserModificationResponse deleteUser(Integer userId, Integer actorUserId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Suggested user was not found"));

        if (user.getRole().equals("OWNER")) {
            throw new InvalidRoleException("Cannot delete the owner account");
        }

        User actor = userRepository.findById(actorUserId).orElse(null);
        Company company = user.getCompany();
        String removedEmail = user.getEmail();


        auditLogRepository.detachUser(userId);
        saleRepository.detachUser(userId);

        this.userRepository.delete(user);

        auditLogService.log(company, actor, AuditLog.EventType.TEAM_MEMBER_REMOVED,
                "Removed team member: " + removedEmail);

        log.info("User deleted: " + userId);
        return new UserModificationResponse(200, new UserDTO(user.getId(), user.getName(), user.getEmail(), user.getRole()));
    }

    public List<UserDTO> searchUser(String searchTerm, Integer companyId) {
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
        log.info("LOOKING FOR COMPANY WITH ID: " + id);
        log.info("existsById(" + id + ") = " + companyRepository.existsById(id));

        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new CompanyNotFoundException("Company not found"));

        return new CompanyDTO(
                company.getId(),
                company.getLogo(),
                company.getEmail(),
                company.getName(),
                company.getIndustry()
        );
    }

    public void uploadImageToSupabase(Company company, MultipartFile image) {
        if (!companyRepository.existsById(company.getId())) {
            throw new CompanyNotFoundException("Company not found");
        }

        if (image == null || image.isEmpty()) {
            company.setLogo(null);
            return;
        }

        String logoUrl = supabaseStorageService.uploadCompanyImage(image);
        company.setLogo(logoUrl);
    }

    public InfoChangeVerificationResponse verifyCompanyUpdateRequest(CompanyDTO suggested) {
        if (!companyRepository.existsById(suggested.id())) {
            throw new CompanyNotFoundException("Company not found");
        }

        String otp = String.valueOf(new SecureRandom().nextInt(900000) + 100000);

        String tempToken = otpStore.save(suggested.id().toString(), suggested.email(), otp);

        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom("netpoint19923@gmail.com");
        message.setTo(suggested.email());
        message.setSubject("Your Business Info Update Request Verification");
        message.setText("Hello! Your verification code is: " + otp +
                "\n\nIt will expire in 5 minutes.");

        mailSender.send(message);
        return new InfoChangeVerificationResponse(200, tempToken);
    }

    public CompanyDTO updateCompanyBusinessInfo(Integer actorUserId, CompanyUpdateRequest suggested,
                                                MultipartFile logo) {
        OtpEntry otpEntry = otpStore.get(suggested.verificationInfo().tempToken());

        if (!otpEntry.getOtpCode().equals(suggested.verificationInfo().otpCode())) {
            throw new InvalidOtpException("Invalid verification code.");
        }

        if (otpEntry.isExpired()) {
            throw new OtpExpiredException("Verification code expired.");
        }
        // ოტპ ვალიდურია და მაინც ინვალიდაციას უკეთებსბ
        otpStore.invalidate(suggested.verificationInfo().tempToken());

        Company company = companyRepository.findById(suggested.id())
                .orElseThrow(() -> new CompanyNotFoundException("Company not found"));

        company.setName(suggested.name());
//        company.setLogo(suggested.logo());
        uploadImageToSupabase(company, logo);
        company.setEmail(suggested.email());
        company.setIndustry(suggested.industry());

        Company saved = companyRepository.save(company);

        User actor = userRepository.findById(actorUserId)
                .orElseThrow(() -> new UserNotFoundException("Acting user was not found"));
        auditLogService.log(saved, actor, AuditLog.EventType.COMPANY_INFO_UPDATED,
                "Company business info updated");

        return new CompanyDTO(
                saved.getId(), saved.getLogo(),
                saved.getEmail(), saved.getName(), saved.getIndustry()
        );
    }
    // accountis daapdeiteba

    @Transactional
    public UserDTO updateAccount(Integer userId, UpdateAccountRequest request) {
        OtpEntry otpEntry = otpStore.get(request.getVerificationInfo().tempToken());

        if (!otpEntry.getOtpCode().equals(request.getVerificationInfo().otpCode())) {

            throw new InvalidOtpException("Invalid verification code.");
        }

        if (otpEntry.isExpired()) {

            throw new OtpExpiredException("Verification code expired.");
        }
        // ოტპ ვალიდურია და მაინც ინვალიდაციას უკეთებსბ
        otpStore.invalidate(request.getVerificationInfo().tempToken());

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (request.getNewInfo().name() != null && !request.getNewInfo().name().isBlank()) {
            user.setName(request.getNewInfo().name());
        }

        if (request.getNewInfo().email() != null && !request.getNewInfo().email().isBlank()) {
            if (userRepository.existsByEmail(request.getNewInfo().email()) &&
                    !user.getEmail().equals(request.getNewInfo().email())) {
                throw new EmailAlreadyExistsException("Email already in use");
            }
            user.setEmail(request.getNewInfo().email());
        }

        if (request.getNewInfo().newPassword() != null && !request.getNewInfo().newPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getNewInfo().newPassword()));
        }

        User updated = userRepository.save(user);

        auditLogService.log(updated.getCompany(), updated, AuditLog.EventType.ACCOUNT_INFO_UPDATED,
                "Account info updated");

        return new UserDTO(updated.getId(), updated.getName(), updated.getEmail(), updated.getRole());
    }

// kompaniis washla, es marto owners sheudzlia

    @Transactional
    public void deleteCompany(Integer actorUserId, Integer companyId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new CompanyNotFoundException("Company not found"));
        User actor = userRepository.findById(actorUserId)
                .orElseThrow(() -> new UserNotFoundException("Acting user was not found"));

        auditLogService.log(company, actor, AuditLog.EventType.COMPANY_DELETED,
                "Company deleted: " + company.getName());

        // jer poulobs kompaniis produqtebis atributebs
        List<ProductAttribute> attributes = productAttributeRepository.findByCompany_Id(companyId);

        for (ProductAttribute attr : attributes) {
            productAttributeValueRepository.deleteByAttributeId(attr.getId());
        }

        //  attributebs, produqtebs da momxmareblebs shlis

        productAttributeRepository.deleteByCompany_Id(companyId);
        productRepository.deleteByCompany_Id(companyId);


        userRepository.deleteByCompany_Id(companyId);

        //bolos imena kompanias shlis
        companyRepository.deleteById(companyId);
    }

    public UserDTO getUserAccountInfo(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User with this id was not found"));

        return new UserDTO(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole()
        );
    }

    public InfoChangeVerificationResponse verifyUserInfoUpdateRequest(UserDTO suggested) {
        User user = userRepository.findById(suggested.id())
                .orElseThrow(() -> new UserNotFoundException("User with this id was not found"));

//        if (suggested.email() != null && !suggested.email().isBlank()) {
        if (!user.getEmail().equals(suggested.email()) && userRepository.existsByEmail(suggested.email())) {
            throw new EmailAlreadyExistsException("A user with this email already exists");
        }
//            user.setEmail(request.getNewInfo().email());
//        }

        String otp = String.valueOf(new SecureRandom().nextInt(900000) + 100000);

        String tempToken = otpStore.save(suggested.id().toString(), suggested.email(), otp);

        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom("netpoint19923@gmail.com");
        message.setTo(suggested.email());
        message.setSubject("Your Personal Info Update Request Verification");
        message.setText("Hello! Your verification code is: " + otp +
                "\n\nIt will expire in 5 minutes.");

        mailSender.send(message);
        return new InfoChangeVerificationResponse(200, tempToken);
    }


}
