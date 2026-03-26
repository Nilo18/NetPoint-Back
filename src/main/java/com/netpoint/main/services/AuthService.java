package com.netpoint.main.services;

import com.netpoint.main.dto.requests.LoginRequest;
import com.netpoint.main.exceptions.EmailAlreadyExistsException;
import com.netpoint.main.models.Company;
import com.netpoint.main.models.User;
import com.netpoint.main.repositories.CompanyRepository;
import com.netpoint.main.repositories.UserRepository;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.java.Log;
import org.hibernate.usertype.UserCollectionType;
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

    public Company signup(Company company) {
        if (this.companyRepository.existsByEmail(company.getEmail())) {
            throw new EmailAlreadyExistsException(company.getEmail());
        }
        company.setPassword(passwordEncoder.encode(company.getPassword()));
        return this.companyRepository.save(company);
    }
}
