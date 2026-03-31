package com.netpoint.main.services;

import com.netpoint.main.dto.requests.CompanyRegistrationRequest;
import com.netpoint.main.dto.responses.CompanySignupResponse;
import com.netpoint.main.dto.requests.LoginRequest;
import com.netpoint.main.exceptions.EmailAlreadyExistsException;
import com.netpoint.main.models.Company;
import com.netpoint.main.repositories.CompanyRepository;
import com.netpoint.main.repositories.UserRepository;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.java.Log;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Log
@AllArgsConstructor
@Data
public class AuthService {
    private UserRepository userRepository;
    private CompanyRepository companyRepository;
    private BCryptPasswordEncoder passwordEncoder;

    public String login(LoginRequest request) {
        log.info("Generating token for: " + request + "...");
        return "token";
    }

    public CompanySignupResponse signup(CompanyRegistrationRequest company) {
        if (this.companyRepository.existsByEmail(company.email())) {
            throw new EmailAlreadyExistsException(company.email());
        }
        Company newCompany = new Company(
                null, company.name(), company.email(), company.industry())
        ;
        newCompany.setPassword(passwordEncoder.encode(company.password()));
        Company savedCompany = this.companyRepository.save(newCompany);
        return new CompanySignupResponse(
                savedCompany.getId(), savedCompany.getName(),
                savedCompany.getEmail(), savedCompany.getIndustry()
        );
    }
}
