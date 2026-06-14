package com.netpoint.main.services;

import com.netpoint.main.dto.requests.CompanyRegistrationRequest;
import com.netpoint.main.dto.requests.VerifyOtpRequest;
import com.netpoint.main.dto.responses.AuthResponse;
import com.netpoint.main.dto.responses.CompanySignupResponse;
import com.netpoint.main.dto.requests.LoginRequest;
import com.netpoint.main.exceptions.*;
import com.netpoint.main.models.Company;
import com.netpoint.main.models.OtpEntry;
import com.netpoint.main.models.PaymentPlan;
import com.netpoint.main.models.User;
import com.netpoint.main.repositories.CompanyRepository;
import com.netpoint.main.repositories.PaymentPlanRepository;
import com.netpoint.main.repositories.UserRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.ArrayList;

// მთელ ავტენტიპიკაციის ლოგიკას ეს უმკლავდება - საიტზე შესვლა, 2ფა და რეგისტრაციაც
@Service
@Log
@AllArgsConstructor
public class AuthService {
    private static final String DEFAULT_PLAN_NAME = "Starter Plan";

    private UserRepository userRepository;
    private CompanyRepository companyRepository;
    private PaymentPlanRepository paymentPlanRepository;
    private BCryptPasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final OtpStore otpStore;
    private final EmailService emailService;
    private final AuthenticationManager authenticationManager;

    // პირველი ნაბიჯია რა საიტზე შესვლის, ქეშიერზე ამოწმებს პაროლს/ლოგინს და თუ სწორია გამოყოფს JWT-ს
    //ადმინზე/მფლობელზე ჯერ ამოწმებს ლოგინ/პაროლს, ქმნის ოტპს, გზავნის ოტპს და აბრუნებს დროებით ტოკენს(tempToken)
    public AuthResponse login(LoginRequest request) {
        log.info("login method is running in AuthService");
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        log.info("Comparing: " + user.getRole() + " to requested role: " + request.role());
        if (!user.getRole().equals(request.role())) {
            throw new InvalidRoleException("Invalid role");
        }

        //ამოწმებს პაროლს იმ ბკრიფტ ჰაშთან
        log.info("The request password is: " + request.password() +
                " The User password is: " + user.getPassword());
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new InvalidPasswordException("Invalid password");
        }
        // agenerirebs 6 cifra kods
        String otp = String.valueOf(new SecureRandom().nextInt(900000) + 100000);

        String tempToken = otpStore.save(user.getId().toString(), user.getEmail(), otp);

        //gzavnis ukve imeilze
        emailService.sendOtpEmail(user.getEmail(), otp);

        log.info("Before returning the response: ");
        return new AuthResponse("2fa_required", tempToken);
    }

    //უკვე მეორე ნაბიჯია, ვერიფიკაციას უკეთებს ოტპს და გასცემს ჯვტს თუ წარმატებულად დამთავრდა
    public AuthResponse verifyOtp(VerifyOtpRequest request) {
        OtpEntry entry = otpStore.get(request.tempToken());
      //თუ ოტპ არასწორია იმწამსვე უარყოფს და შლის
        if (!entry.getOtpCode().equals(request.otpCode())) {
            otpStore.invalidate(request.tempToken());
            throw new InvalidOtpException("Invalid verification code.");
        }

        log.info("Checking entry.isExpired() value: " + entry.isExpired());
        if (entry.isExpired()) {
            log.info("Throwing verification code expired error...");
            throw new OtpExpiredException("Verification code expired.");
        }
        // ოტპ ვალიდურია და მაინც ინვალიდაციას უკეთებსბ
        otpStore.invalidate(request.tempToken());
        //იუზერს იძახებს რო მისი როლი გაიგოს
        User user = userRepository.findById(Integer.parseInt(entry.getUserId()))
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        //ჯვტს გადასცემს იუზერს სწორი როლით
        String jwt = jwtService.generateToken(
                entry.getUserId(), String.valueOf(user.getCompany().getId()),
                user.getName(), user.getEmail(), user.getRole()
        );
        return new AuthResponse("authenticated", jwt);
    }

    @Transactional
    public CompanySignupResponse signup(CompanyRegistrationRequest company) {
        if (this.companyRepository.existsByEmail(company.email())) {
            throw new EmailAlreadyExistsException(company.email());
        }

        if (this.userRepository.existsByEmail(company.owner_email())) {
            throw new EmailAlreadyExistsException("User with this email already exists");
        }

        PaymentPlan defaultPlan = paymentPlanRepository.findByPlanName(DEFAULT_PLAN_NAME)
                .orElseThrow(() -> new PaymentPlanNotFoundException("Default plan not found"));

        Company newCompany = new Company();
        newCompany.setName(company.name());
        newCompany.setEmail(company.email());
        newCompany.setPassword(passwordEncoder.encode(company.password()));
        newCompany.setIndustry(company.industry());
        newCompany.setPlan(defaultPlan);

        Company savedCompany = this.companyRepository.save(newCompany);



        User user = new User();
        user.setCompany(savedCompany);
        user.setName(company.owner_name());
        user.setEmail(company.owner_email());
        user.setPassword(passwordEncoder.encode(company.owner_password()));
        user.setRole(company.role());
//        user.setPin(company.phone_number());  // assuming pin field holds phone number

        User savedUser = this.userRepository.save(user);


        String accessToken = this.jwtService.generateToken(
                savedUser.getId().toString(), String.valueOf(user.getCompany().getId()),
                savedUser.getName(), savedCompany.getEmail(), savedUser.getRole()
        );

        return new CompanySignupResponse(200, accessToken);
    }
}
