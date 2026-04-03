package com.netpoint.main.services;

import com.netpoint.main.dto.requests.CompanyRegistrationRequest;
import com.netpoint.main.dto.requests.VerifyOtpRequest;
import com.netpoint.main.dto.responses.AuthResponse;
import com.netpoint.main.dto.responses.CompanySignupResponse;
import com.netpoint.main.dto.requests.LoginRequest;
import com.netpoint.main.exceptions.EmailAlreadyExistsException;
import com.netpoint.main.models.Company;
import com.netpoint.main.models.OtpEntry;
import com.netpoint.main.models.User;
import com.netpoint.main.repositories.CompanyRepository;
import com.netpoint.main.repositories.UserRepository;
import lombok.AllArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;


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



    // ესაა პირველი ნაბიჯი რა საიტზე შესვლის
    // CASHIER: სწორი პაროლი/ლოგინი და ეგაა შევა
    // ADMIN/OWNER: სწორი პაროლი/ლოგინი, იქმნება OTP, იგზავნება ტელეზე, აბრუნებს tempToken
    public AuthResponse login(LoginRequest request) {
        // მომხმარებლის იმეილი რო არსებობს ამოწმებს და throw-ს აკეთებს თუ ვერ იპოვა
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        //ამოწმებს ხო სწორი პაროლიაო
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        //ქეშიერებისაა პირველი ნაწილი, პირდაპირ შედიან
        if(user.getRole().equals("CASHIER")){
            String jwt = jwtService.generateToken(user.getId().toString(), user.getRole());
            return new AuthResponse("authenticated", jwt);
        }//ეს უკვე ადმინი/მეპატრონე ამათ უკვე უნდათ 2FA
        else if (user.getRole().equals("ADMIN") || user.getRole().equals("OWNER")){
            //ეს აგენერირებს იმ 2ფა-ს კოდს
            String otp = String.valueOf(new SecureRandom().nextInt(900000) + 100000);
            //5 წუთი ინახავს იმ შექმნილ კოდს და ტემპ ტოკენს იღებს
            String tempToken = otpStore.save(user.getId().toString(), user.getPhoneNumber(), otp);
            //გზავნის ამ ოტპს ნომერზე
            smsService.sendOtp(user.getPhoneNumber(), otp);
            return new AuthResponse("2fa_required", tempToken);

        }
        //ეს არის მაგრამ ალბათ არ დაგვჭირდება არასდროს
        throw new RuntimeException("Invalid role");
    }

    // ეს უკვე ამოწმებს 2ფა-ს კოდს და JWT-ს გზავნის თუ სწორია
    public AuthResponse verifyOtp(VerifyOtpRequest request) {

        OtpEntry entry = otpStore.get(request.getTempToken());
        //თუ ოტპ არ ემთხვევა იმწამსვე უარყოფს და ინვალიდაციას უკეთებს
        if (!entry.getOtpCode().equals(request.getOtpCode())) {
            otpStore.invalidate(request.getTempToken());
            throw new RuntimeException("Invalid OTP");
        }
        //ეს როცა ოტპ სწორია
        otpStore.invalidate(request.getTempToken());

        // იუზერს იძახებს რომ მათ როლი მიიღოს JWT -სთვის
        User user = userRepository.findById(Integer.parseInt(entry.getUserId()))
                .orElseThrow(() -> new RuntimeException("User not found"));
        //JWT-ს გასცემს მომხმარებლის როლით
        String jwt = jwtService.generateToken(entry.getUserId(), user.getRole());
        return new AuthResponse("authenticated", jwt);

    }

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
        return new CompanySignupResponse(
                savedCompany.getId(), savedCompany.getName(),
                savedCompany.getEmail(), savedCompany.getIndustry()
        );
    }



}
