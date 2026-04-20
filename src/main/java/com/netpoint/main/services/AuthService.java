package com.netpoint.main.services;

import com.netpoint.main.dto.requests.CompanyRegistrationRequest;
import com.netpoint.main.dto.requests.VerifyOtpRequest;
import com.netpoint.main.dto.responses.AuthResponse;
import com.netpoint.main.dto.responses.CompanySignupResponse;
import com.netpoint.main.dto.requests.LoginRequest;
import com.netpoint.main.exceptions.EmailAlreadyExistsException;
import com.netpoint.main.exceptions.InvalidPasswordException;
import com.netpoint.main.exceptions.InvalidRoleException;
import com.netpoint.main.exceptions.UserNotFoundException;
import com.netpoint.main.models.Company;
import com.netpoint.main.models.OtpEntry;
import com.netpoint.main.models.User;
import com.netpoint.main.repositories.CompanyRepository;
import com.netpoint.main.repositories.UserRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;

// მთელ ავტენტიპიკაციის ლოგიკას ეს უმკლავდება - საიტზე შესვლა, 2ფა და რეგისტრაციაც
@Service
@Log
@AllArgsConstructor
public class AuthService {
    private UserRepository userRepository;
    private CompanyRepository companyRepository;
    private BCryptPasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final OtpStore otpStore;
    private final SmsService smsService;
    private final AuthenticationManager authenticationManager;

    // პირველი ნაბიჯია რა საიტზე შესვლის, ქეშიერზე ამოწმებს პაროლს/ლოგინს და თუ სწორია გამოყოფს JWT-ს
    //ადმინზე/მფლობელზე ჯერ ამოწმებს ლოგინ/პაროლს, ქმნის ოტპს, გზავნის ოტპს და აბრუნებს დროებით ტოკენს(tempToken)
    public AuthResponse login(LoginRequest request) {
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
        //ქეშიერები პირდაპირ იღებენ ჯვტს და არანაირი 2ფა
        if(user.getRole().equals("CASHIER")){
            String jwt = jwtService.generateToken(
                    user.getId().toString(), user.getEmail(), user.getName(), user.getRole()
            );
            return new AuthResponse("authenticated", jwt);
        }//2ფას ლოგიკა უკვე ადმინისთვის და მფლობელისთვის
        else if (user.getRole().equals("ADMIN") || user.getRole().equals("OWNER")){
            //ქმნის რაღაც უსაფრთხო 6 ციფრა კოდს რაც მესიჯად მიდის ტელეზე(ოტპ)
            String otp = String.valueOf(new SecureRandom().nextInt(900000) + 100000);
            //5 წუთის მანძილზე მეხსიერებაში ინახავს მაგ ოტპს და აბრუნებს ტემპტოკენს
            String tempToken = otpStore.save(user.getId().toString(), user.getPhoneNumber(), otp);
            //ტვილიოთი აგზავნის ოტპს
            smsService.sendOtp(user.getPhoneNumber(), otp);
            // აბრუნებს ტემპტოკენს რაც გჭირდება 2ფასთვის
            return new AuthResponse("2fa_required", tempToken);
        }
        // ეს არის, მაგრამ აქამდე წესით არ მოვა არასდროს
        throw new InvalidRoleException("Invalid role");
    }
    //უკვე მეორე ნაბიჯია, ვერიფიკაციას უკეთებს ოტპს და გასცემს ჯვტს თუ წარმატებულად დამთავრდა
    public AuthResponse verifyOtp(VerifyOtpRequest request) {
            OtpEntry entry = otpStore.get(request.tempToken());
      //თუ ოტპ არასწორია იმწამსვე უარყოფს და შლის
        if (!entry.getOtpCode().equals(request.otpCode())) {
            otpStore.invalidate(request.tempToken());
            throw new RuntimeException("Invalid OTP");
        }
        // ოტპ ვალიდურია და მაინც ინვალიდაციას უკეთებსბ
        otpStore.invalidate(request.tempToken());
        //იუზერს იძახებს რო მისი როლი გაიგოს
        User user = userRepository.findById(Integer.parseInt(entry.getUserId()))
                .orElseThrow(() -> new RuntimeException("User not found"));
        //ჯვტს გადასცემს იუზერს სწორი როლით
        String jwt = jwtService.generateToken(
                entry.getUserId(), user.getEmail(), user.getName(), user.getRole()
        );
        return new AuthResponse("authenticated", jwt);

    }

    @Transactional
    public CompanySignupResponse signup(CompanyRegistrationRequest company) {
        if (this.companyRepository.existsByEmail(company.email())) {
            throw new EmailAlreadyExistsException(company.email());
        }

        Company newCompany = new Company(
                null,
                company.name(),
                company.email(),
                passwordEncoder.encode(company.password()),
                company.industry()
        );

        Company savedCompany = this.companyRepository.save(newCompany);

        User user = new User(
                null,
                savedCompany.getId(),
                company.owner_name(),
                company.owner_email(),
                passwordEncoder.encode(company.owner_password()),
                company.role(),
                company.phone_number()
        );

        User savedUser = this.userRepository.save(user);

        String accessToken = this.jwtService.generateToken(
                savedUser.getId().toString(), savedUser.getName(), savedCompany.getEmail(), savedUser.getRole()
        );

        return new CompanySignupResponse(200, accessToken);
    }
}
