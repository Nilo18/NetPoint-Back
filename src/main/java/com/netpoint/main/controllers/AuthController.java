package com.netpoint.main.controllers;

import com.netpoint.main.dto.requests.CompanyRegistrationRequest;
import com.netpoint.main.dto.responses.CompanySignupResponse;
import com.netpoint.main.dto.requests.LoginRequest;
import com.netpoint.main.services.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path="/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping(path="/login")
    public String login(@Valid @RequestBody LoginRequest request) {
        return this.authService.login(request);
    }

    @PostMapping(path="/signup")
    public ResponseEntity<CompanySignupResponse> signup(@Valid @RequestBody CompanyRegistrationRequest company) {
        CompanySignupResponse created = this.authService.signup(company);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
}
